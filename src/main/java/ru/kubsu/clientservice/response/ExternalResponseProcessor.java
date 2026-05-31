package ru.kubsu.clientservice.response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.ExternalResponse;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.entity.Response;
import ru.kubsu.clientservice.entity.ResponseBatch;
import ru.kubsu.clientservice.repository.BatchRequestRepository;
import ru.kubsu.clientservice.repository.ExternalRequestRepository;
import ru.kubsu.clientservice.repository.ExternalResponseRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.repository.ResponseBatchRepository;
import ru.kubsu.clientservice.repository.ResponseRepository;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Обработка пачки ответа ведомства: сохранение сущностей, обновление клиентов и статусов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalResponseProcessor {

    /** Репозиторий пакетных запросов. */
    private final BatchRequestRepository batchRequestRepository;

    /** Репозиторий запросов по клиентам. */
    private final RequestRepository requestRepository;

    /** Репозиторий внешних запросов. */
    private final ExternalRequestRepository externalRequestRepository;

    /** Репозиторий пачек ответов. */
    private final ResponseBatchRepository responseBatchRepository;

    /** Репозиторий внешних ответов. */
    private final ExternalResponseRepository externalResponseRepository;

    /** Репозиторий данных клиента из ответа. */
    private final ResponseRepository responseRepository;

    /** Сравнение данных клиента с ответом. */
    private final ClientResponseComparator clientResponseComparator;

    /** Обновление клиента по ответу. */
    private final ClientResponseUpdater clientResponseUpdater;

    /**
     * Обрабатывает пачку ответа ведомства.
     *
     * @param message сообщение ответа
     */
    @Transactional
    public void process(ExternalResponseBatchMessage message) {
        BatchRequest batchRequest = batchRequestRepository.findById(message.getBatchRequestId())
                .orElse(null);
        if (batchRequest == null) {
            log.warn("Пачка ответа с неизвестным batchRequestId={} проигнорирована",
                    message.getBatchRequestId());
            return;
        }

        ResponseBatch responseBatch = saveResponseBatch(batchRequest, message);
        List<Request> batchRequests = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequest.getId());
        Map<UUID, Request> requestsByCorrelationId = batchRequests.stream()
                .collect(Collectors.toMap(Request::getMessageId, Function.identity()));

        Set<UUID> respondedCorrelationIds = new HashSet<>();
        List<ExternalResponseItemMessage> items = message.getItems() != null ? message.getItems() : List.of();

        for (ExternalResponseItemMessage item : items) {
            if (item.getCorrelationId() == null) {
                continue;
            }
            Request request = requestsByCorrelationId.get(item.getCorrelationId());
            if (request == null) {
                log.warn("Ответ с неизвестным correlationId={} в пачке batchRequestId={} проигнорирован",
                        item.getCorrelationId(), message.getBatchRequestId());
                continue;
            }
            respondedCorrelationIds.add(item.getCorrelationId());
            processFoundItem(responseBatch, request, item);
        }

        for (Request request : batchRequests) {
            if (!respondedCorrelationIds.contains(request.getMessageId())) {
                processNotFoundItem(responseBatch, request);
            }
        }

        tryCompleteExternalRequest(batchRequest.getExternalRequest().getId());
    }

    /**
     * Сохраняет пачку ответа.
     *
     * @param batchRequest пакетный запрос
     * @param message      сообщение ответа
     * @return сохранённая пачка ответа
     */
    private ResponseBatch saveResponseBatch(BatchRequest batchRequest, ExternalResponseBatchMessage message) {
        ResponseBatch responseBatch = new ResponseBatch()
                .setBatchRequest(batchRequest)
                .setSourceType(message.getSourceType())
                .setMessageId(message.getBatchMessageId())
                .setReceivedDate(OffsetDateTime.now());
        return responseBatchRepository.save(responseBatch);
    }

    /**
     * Обрабатывает найденный элемент ответа.
     *
     * @param responseBatch пачка ответа
     * @param request       исходный запрос
     * @param item          элемент ответа
     */
    private void processFoundItem(ResponseBatch responseBatch,
                                  Request request,
                                  ExternalResponseItemMessage item) {
        ExternalResponse externalResponse = saveExternalResponse(responseBatch, request, item);
        Response responseEntity = clientResponseUpdater.toResponseEntity(item);
        responseEntity.setExternalResponse(externalResponse);
        responseRepository.save(responseEntity);

        RequestOutcome outcome = resolveFoundOutcome(request, item);
        request.setStatus(RequestStatus.DONE);
        request.setOutcome(outcome);
        requestRepository.save(request);

        externalResponse.setStatus(RequestStatus.DONE);
        externalResponseRepository.save(externalResponse);
    }

    /**
     * Обрабатывает запрос, отсутствующий в ответе ведомства.
     *
     * @param responseBatch пачка ответа
     * @param request       исходный запрос
     */
    private void processNotFoundItem(ResponseBatch responseBatch, Request request) {
        ExternalResponse externalResponse = new ExternalResponse()
                .setResponseBatch(responseBatch)
                .setRequest(request)
                .setCorrelationId(request.getMessageId())
                .setClient(request.getClient())
                .setStatus(RequestStatus.DONE);
        externalResponseRepository.save(externalResponse);

        request.setStatus(RequestStatus.DONE);
        request.setOutcome(RequestOutcome.NOT_FOUND);
        requestRepository.save(request);
    }

    /**
     * Сохраняет внешний ответ по найденному элементу.
     *
     * @param responseBatch пачка ответа
     * @param request       исходный запрос
     * @param item          элемент ответа
     * @return сохранённый внешний ответ
     */
    private ExternalResponse saveExternalResponse(ResponseBatch responseBatch,
                                                  Request request,
                                                  ExternalResponseItemMessage item) {
        ExternalResponse externalResponse = new ExternalResponse()
                .setResponseBatch(responseBatch)
                .setRequest(request)
                .setCorrelationId(item.getCorrelationId())
                .setClient(request.getClient())
                .setStatus(RequestStatus.RECEIVED);
        return externalResponseRepository.save(externalResponse);
    }

    /**
     * Определяет итог обработки найденного запроса.
     *
     * @param request исходный запрос
     * @param item    элемент ответа
     * @return итог обработки
     */
    private RequestOutcome resolveFoundOutcome(Request request, ExternalResponseItemMessage item) {
        Client client = request.getClient();
        if (client == null) {
            return clientResponseUpdater.manualRequestMatchesResponse(request, item)
                    ? RequestOutcome.ACTUAL
                    : RequestOutcome.UPDATED;
        }
        if (clientResponseComparator.needsUpdate(client, item)) {
            clientResponseUpdater.updateFromResponseItem(client, item);
            return RequestOutcome.UPDATED;
        }
        return RequestOutcome.ACTUAL;
    }

    /**
     * Переводит внешний запрос в DONE, если все его запросы завершены.
     *
     * @param externalRequestId идентификатор внешнего запроса
     */
    private void tryCompleteExternalRequest(UUID externalRequestId) {
        long incompleteCount = requestRepository.countByBatchRequest_ExternalRequest_IdAndStatusNot(
                externalRequestId, RequestStatus.DONE);
        if (incompleteCount > 0) {
            return;
        }

        ExternalRequest externalRequest = externalRequestRepository.findById(externalRequestId)
                .orElse(null);
        if (externalRequest != null && externalRequest.getStatus() != RequestStatus.ERROR) {
            externalRequest.setStatus(RequestStatus.DONE);
        }
    }
}
