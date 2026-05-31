package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.outbox.OutboxPublishProcessor;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.repository.ExternalRequestRepository;
import ru.kubsu.clientservice.repository.ExternalResponseRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.repository.ResponseRepository;
import ru.kubsu.clientservice.response.ExternalResponseProcessor;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.ExternalRequestTestFixtures;
import ru.kubsu.clientservice.support.ExternalResponseTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты обработки ответов ведомств.
 */
class ExternalResponseProcessorIT extends IntegrationTestBase {

    /** Обработчик пачки ответа. */
    @Autowired
    private ExternalResponseProcessor externalResponseProcessor;

    /** Обработчик публикации outbox. */
    @Autowired
    private OutboxPublishProcessor outboxPublishProcessor;

    /** Репозиторий клиентов. */
    @Autowired
    private ClientRepository clientRepository;

    /** Репозиторий запросов. */
    @Autowired
    private RequestRepository requestRepository;

    /** Репозиторий внешних запросов. */
    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    /** Репозиторий внешних ответов. */
    @Autowired
    private ExternalResponseRepository externalResponseRepository;

    /** Репозиторий данных ответа. */
    @Autowired
    private ResponseRepository responseRepository;

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /**
     * Проверяет обновление клиента при ответе с новыми данными.
     */
    @Test
    void should_updateClient_whenResponseHasNewerData() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createAndPublishExternalRequest(List.of(clientId));

        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        UUID correlationId = externalRequest.getBatches().getFirst().getRequests().getFirst().getMessageId();
        ExternalResponseBatchMessage response = ExternalResponseTestFixtures.updatedResponse(batchRequestId, correlationId);

        externalResponseProcessor.process(response);

        Client client = clientRepository.findById(clientId).orElseThrow();
        assertThat(client.getLastName()).isEqualTo("Петров");

        Request request = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).getFirst();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.DONE);
        assertThat(request.getOutcome()).isEqualTo(RequestOutcome.UPDATED);

        assertThat(externalRequestRepository.findById(externalRequest.getId()))
                .isPresent()
                .get()
                .extracting(er -> er.getStatus())
                .isEqualTo(RequestStatus.DONE);

        assertThat(countExternalResponses(batchRequestId)).isEqualTo(1);
        assertThat(responseRepository.findAll()).hasSize(1);
    }

    /**
     * Проверяет итог ACTUAL, если данные клиента уже совпадают с ответом.
     */
    @Test
    void should_markActual_whenResponseMatchesClient() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createAndPublishExternalRequest(List.of(clientId));

        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        UUID correlationId = externalRequest.getBatches().getFirst().getRequests().getFirst().getMessageId();
        ExternalResponseBatchMessage response = ExternalResponseTestFixtures.matchingResponse(batchRequestId, correlationId);

        externalResponseProcessor.process(response);

        Request request = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).getFirst();
        assertThat(request.getOutcome()).isEqualTo(RequestOutcome.ACTUAL);
        assertThat(clientRepository.findById(clientId).orElseThrow().getLastName()).isEqualTo("Иванов");
    }

    /**
     * Проверяет итог NOT_FOUND, если correlationId отсутствует в ответе.
     */
    @Test
    void should_markNotFound_whenCorrelationIdMissingInResponse() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createAndPublishExternalRequest(List.of(clientId));

        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        externalResponseProcessor.process(ExternalResponseTestFixtures.emptyResponse(batchRequestId));

        Request request = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).getFirst();
        assertThat(request.getOutcome()).isEqualTo(RequestOutcome.NOT_FOUND);
        assertThat(countResponses(batchRequestId)).isZero();
    }

    /**
     * Проверяет, что неизвестный correlationId в ответе игнорируется.
     */
    @Test
    void should_ignoreUnknownCorrelationId_inResponseBatch() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createAndPublishExternalRequest(List.of(clientId));

        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        UUID correlationId = externalRequest.getBatches().getFirst().getRequests().getFirst().getMessageId();
        ExternalResponseBatchMessage response = ExternalResponseTestFixtures.matchingResponse(batchRequestId, correlationId);
        List<ExternalResponseItemMessage> items = new ArrayList<>(response.getItems());
        items.add(new ExternalResponseItemMessage().setCorrelationId(UUID.randomUUID())
                .setFirstName("Unknown")
                .setLastName("Unknown")
                .setBirthDate(java.time.LocalDate.of(1980, 1, 1))
                .setGender(ru.kubsu.contracts.enums.service.client.Gender.MALE)
                .setIdentityDocumentSeries("0000")
                .setIdentityDocumentNumber("000000")
                .setIdentityDocumentIssueDate(java.time.LocalDate.of(2000, 1, 1))
                .setResidenceAddressName("unknown"));
        response.setItems(items);

        externalResponseProcessor.process(response);

        assertThat(countExternalResponses(batchRequestId)).isEqualTo(1);
        assertThat(requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).getFirst().getOutcome())
                .isEqualTo(RequestOutcome.ACTUAL);
    }

    /**
     * Подсчитывает external_response для пакетного запроса.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @return количество записей
     */
    private long countExternalResponses(UUID batchRequestId) {
        List<UUID> requestIds = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).stream()
                .map(Request::getId)
                .toList();
        return externalResponseRepository.findAll().stream()
                .filter(response -> requestIds.contains(response.getRequest().getId()))
                .count();
    }

    /**
     * Подсчитывает response для пакетного запроса.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @return количество записей
     */
    private long countResponses(UUID batchRequestId) {
        List<UUID> requestIds = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId).stream()
                .map(Request::getId)
                .toList();
        return responseRepository.findAll().stream()
                .filter(response -> requestIds.contains(response.getExternalResponse().getRequest().getId()))
                .count();
    }

    /**
     * Проверяет summary в GET /external-requests/{id}.
     */
    @Test
    void should_returnSummary_whenExternalRequestLoadedById() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createAndPublishExternalRequest(List.of(clientId));

        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        UUID correlationId = externalRequest.getBatches().getFirst().getRequests().getFirst().getMessageId();
        externalResponseProcessor.process(
                ExternalResponseTestFixtures.updatedResponse(batchRequestId, correlationId));

        mockMvc.perform(get("/external-requests/{id}", externalRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.updatedCount").value(1))
                .andExpect(jsonPath("$.summary.pendingCount").value(0))
                .andExpect(jsonPath("$.batches[0].requests[0].outcome").value("UPDATED"));
    }

    /**
     * Создаёт внешний запрос, публикует outbox и возвращает агрегированный ответ.
     *
     * @param clientIds идентификаторы клиентов
     * @return внешний запрос
     */
    private ExternalRequestTo createAndPublishExternalRequest(List<UUID> clientIds) throws Exception {
        ExternalRequestBatchCreateRequest request = ExternalRequestTestFixtures.validBatchCreateRequest(clientIds);
        String responseJson = mockMvc.perform(post("/external-requests/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExternalRequestTo externalRequest = jsonMapper.readValue(responseJson, ExternalRequestTo.class);
        outboxPublishProcessor.publishPendingMessages();
        return externalRequest;
    }
}
