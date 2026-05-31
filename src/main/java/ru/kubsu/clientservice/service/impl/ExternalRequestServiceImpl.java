package ru.kubsu.clientservice.service.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.config.BatchingProperties;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.mapper.ExternalRequestMapper;
import ru.kubsu.clientservice.mapper.RequestMapper;
import ru.kubsu.clientservice.outbox.OutboxWriter;
import ru.kubsu.clientservice.query.ExternalRequestPageableFactory;
import ru.kubsu.clientservice.query.ExternalRequestPredicateBuilder;
import ru.kubsu.clientservice.query.ExternalRequestSortParser;
import ru.kubsu.clientservice.query.ExternalRequestSummaryBuilder;
import ru.kubsu.clientservice.query.PageDataBuilder;
import ru.kubsu.clientservice.repository.BatchRequestRepository;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.repository.ExternalRequestRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.ExternalRequestService;
import ru.kubsu.contracts.dto.service.client.BatchRequestTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestListTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestManualCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestQueryParams;
import ru.kubsu.contracts.dto.service.client.ExternalRequestSummaryTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.dto.service.client.PageData;
import ru.kubsu.contracts.dto.service.client.RequestTo;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.enums.service.client.SourceType;
import ru.kubsu.contracts.exception.service.client.ClientNotFoundException;
import ru.kubsu.contracts.exception.service.client.ExternalRequestNotFoundException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса бизнес-логики работы с внешними запросами.
 */
@Service
@RequiredArgsConstructor
public class ExternalRequestServiceImpl implements ExternalRequestService {

    /** Репозиторий внешних запросов. */
    private final ExternalRequestRepository externalRequestRepository;

    /** Репозиторий пакетных запросов. */
    private final BatchRequestRepository batchRequestRepository;

    /** Репозиторий запросов по клиентам. */
    private final RequestRepository requestRepository;

    /** Репозиторий клиентов. */
    private final ClientRepository clientRepository;

    /** MapStruct-маппер внешних запросов. */
    private final ExternalRequestMapper externalRequestMapper;

    /** MapStruct-маппер запросов. */
    private final RequestMapper requestMapper;

    /** Запись outbox-сообщений. */
    private final OutboxWriter outboxWriter;

    /** Свойства разбиения на пачки. */
    private final BatchingProperties batchingProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExternalRequestTo createBatch(ExternalRequestBatchCreateRequest request) {
        List<UUID> clientIds = request.getClientIds().stream().distinct().toList();
        List<Client> clients = loadClients(clientIds);

        ExternalRequest externalRequest = saveExternalRequest(
                request.getLetterNumber(),
                request.getLetterDate(),
                request.getSourceType(),
                request.getInitiatorLogin());

        for (List<Client> chunk : partition(clients, batchingProperties.getSize())) {
            persistBatchWithRequests(externalRequest, chunk);
        }

        return loadAggregated(externalRequest.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExternalRequestTo createManual(ExternalRequestManualCreateRequest request) {
        ExternalRequest externalRequest = saveExternalRequest(
                request.getLetterNumber(),
                request.getLetterDate(),
                request.getSourceType(),
                request.getInitiatorLogin());

        BatchRequest batchRequest = createBatchRequest(externalRequest, 1);
        batchRequest = batchRequestRepository.save(batchRequest);

        UUID correlationId = UUID.randomUUID();
        Request manualRequest = requestMapper.fromManual(request, batchRequest, correlationId);
        manualRequest = requestRepository.save(manualRequest);

        outboxWriter.writeExternalRequestBatchEvent(
                externalRequest, batchRequest, List.of(manualRequest));

        return loadAggregated(externalRequest.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BatchRequestTo> getBatches(UUID id) {
        if (!externalRequestRepository.existsById(id)) {
            throw new ExternalRequestNotFoundException(id);
        }
        return loadBatches(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ExternalRequestTo getById(UUID id) {
        if (!externalRequestRepository.existsById(id)) {
            throw new ExternalRequestNotFoundException(id);
        }
        return loadAggregated(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageData<ExternalRequestListTo> search(ExternalRequestQueryParams params) {
        Predicate predicate = ExternalRequestPredicateBuilder.from(params);
        OrderSpecifier<?> orderSpecifier = ExternalRequestSortParser.parse(params.getSortKey());
        Pageable pageable = ExternalRequestPageableFactory.from(params, orderSpecifier);

        Page<ExternalRequest> page = externalRequestRepository.findAll(predicate, pageable);
        List<UUID> externalRequestIds = page.getContent().stream()
                .map(ExternalRequest::getId)
                .toList();

        Map<UUID, Long> batchCounts = loadBatchCounts(externalRequestIds);
        Map<UUID, Long> requestCounts = loadRequestCounts(externalRequestIds);

        return PageDataBuilder.from(page, externalRequest -> toListTo(
                externalRequest,
                batchCounts.getOrDefault(externalRequest.getId(), 0L),
                requestCounts.getOrDefault(externalRequest.getId(), 0L)));
    }

    /**
     * Загружает клиентов по списку идентификаторов.
     *
     * @param clientIds идентификаторы клиентов
     * @return список клиентов
     */
    private List<Client> loadClients(List<UUID> clientIds) {
        List<Client> clients = clientRepository.findAllById(clientIds);
        if (clients.size() != clientIds.size()) {
            Set<UUID> foundIds = clients.stream()
                    .map(Client::getId)
                    .collect(Collectors.toSet());
            UUID missingId = clientIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();
            throw new ClientNotFoundException(missingId);
        }
        return clients;
    }

    /**
     * Сохраняет внешний запрос в статусе PROCESSING.
     *
     * @param letterNumber   номер письма
     * @param letterDate     дата письма
     * @param sourceType     тип источника
     * @param initiatorLogin логин инициатора
     * @return сохранённый внешний запрос
     */
    private ExternalRequest saveExternalRequest(String letterNumber,
                                                 LocalDate letterDate,
                                                 SourceType sourceType,
                                                 String initiatorLogin) {
        ExternalRequest externalRequest = new ExternalRequest()
                .setLetterNumber(letterNumber)
                .setLetterDate(letterDate)
                .setSourceType(sourceType)
                .setInitiatorLogin(initiatorLogin)
                .setStatus(RequestStatus.PROCESSING)
                .setCreated(OffsetDateTime.now());
        return externalRequestRepository.save(externalRequest);
    }

    /**
     * Сохраняет пачку с запросами и записывает outbox-событие.
     *
     * @param externalRequest внешний запрос
     * @param clients         клиенты пачки
     */
    private void persistBatchWithRequests(ExternalRequest externalRequest, List<Client> clients) {
        BatchRequest batchRequest = createBatchRequest(externalRequest, clients.size());
        batchRequest = batchRequestRepository.save(batchRequest);

        List<Request> requests = new ArrayList<>();
        for (Client client : clients) {
            Request request = requestMapper.fromClient(client, batchRequest);
            requests.add(requestRepository.save(request));
        }

        outboxWriter.writeExternalRequestBatchEvent(externalRequest, batchRequest, requests);
    }

    /**
     * Создаёт сущность пакетного запроса.
     *
     * @param externalRequest внешний запрос
     * @param messageCount    количество записей в пачке
     * @return новая сущность пакетного запроса
     */
    private BatchRequest createBatchRequest(ExternalRequest externalRequest, int messageCount) {
        return new BatchRequest()
                .setExternalRequest(externalRequest)
                .setMessageId(UUID.randomUUID())
                .setCreatedDate(OffsetDateTime.now())
                .setMessageCount(messageCount);
    }

    /**
     * Загружает агрегированный внешний запрос с пачками и запросами.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @return агрегированный транспортный объект
     */
    private ExternalRequestTo loadAggregated(UUID externalRequestId) {
        ExternalRequest externalRequest = externalRequestRepository.findById(externalRequestId)
                .orElseThrow(() -> new ExternalRequestNotFoundException(externalRequestId));

        List<BatchRequestTo> batchTos = loadBatches(externalRequestId);

        List<RequestTo> allRequests = batchTos.stream()
                .flatMap(batch -> batch.getRequests().stream())
                .toList();
        ExternalRequestSummaryTo summary = ExternalRequestSummaryBuilder.fromRequests(allRequests);

        return externalRequestMapper.toExternalRequestTo(externalRequest)
                .setBatches(batchTos)
                .setSummary(summary);
    }

    /**
     * Загружает пакетные запросы с вложенными запросами по клиентам.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @return список пакетных запросов
     */
    private List<BatchRequestTo> loadBatches(UUID externalRequestId) {
        return batchRequestRepository.findByExternalRequest_IdOrderByCreatedDateAsc(externalRequestId).stream()
                .map(batch -> {
                    List<RequestTo> requestTos = requestRepository.findByBatchRequest_IdOrderByIdAsc(batch.getId())
                            .stream()
                            .map(requestMapper::toRequestTo)
                            .toList();
                    return externalRequestMapper.toBatchRequestTo(batch).setRequests(requestTos);
                })
                .toList();
    }

    /**
     * Формирует краткий транспортный объект с количеством пачек и запросов.
     *
     * @param externalRequest внешний запрос
     * @param batchCount      количество пачек
     * @param requestCount    количество запросов
     * @return краткий транспортный объект
     */
    private ExternalRequestListTo toListTo(ExternalRequest externalRequest,
                                           long batchCount,
                                           long requestCount) {
        return externalRequestMapper.toExternalRequestListTo(externalRequest)
                .setBatchCount(batchCount)
                .setRequestCount(requestCount);
    }

    /**
     * Загружает количество пачек по идентификаторам внешних запросов.
     *
     * @param externalRequestIds идентификаторы внешних запросов
     * @return карта externalRequestId → count
     */
    private Map<UUID, Long> loadBatchCounts(List<UUID> externalRequestIds) {
        if (externalRequestIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : batchRequestRepository.countByExternalRequestIds(externalRequestIds)) {
            counts.put((UUID) row[0], (Long) row[1]);
        }
        return counts;
    }

    /**
     * Загружает количество запросов по идентификаторам внешних запросов.
     *
     * @param externalRequestIds идентификаторы внешних запросов
     * @return карта externalRequestId → count
     */
    private Map<UUID, Long> loadRequestCounts(List<UUID> externalRequestIds) {
        if (externalRequestIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : requestRepository.countByExternalRequestIds(externalRequestIds)) {
            counts.put((UUID) row[0], (Long) row[1]);
        }
        return counts;
    }

    /**
     * Разбивает список на части заданного размера.
     *
     * @param items элементы
     * @param size  размер части
     * @param <T>   тип элементов
     * @return список частей
     */
    private static <T> List<List<T>> partition(List<T> items, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int index = 0; index < items.size(); index += size) {
            result.add(items.subList(index, Math.min(index + size, items.size())));
        }
        return result;
    }
}
