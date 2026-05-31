package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import ru.kubsu.clientservice.outbox.OutboxPublishProcessor;
import ru.kubsu.clientservice.response.ExternalResponseProcessor;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.ExternalRequestTestFixtures;
import ru.kubsu.clientservice.support.ExternalResponseTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты read-эндпоинтов этапа 6.
 */
class ExternalRequestReadIT extends IntegrationTestBase {

    /** Обработчик публикации outbox. */
    @Autowired
    private OutboxPublishProcessor outboxPublishProcessor;

    /** Обработчик ответов ведомств. */
    @Autowired
    private ExternalResponseProcessor externalResponseProcessor;

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /**
     * Проверяет GET /external-requests/{id}/batches.
     */
    @Test
    void should_returnBatches_whenExternalRequestExists() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createExternalRequest(List.of(clientId));

        mockMvc.perform(get("/external-requests/{id}/batches", externalRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].requests[0].clientId").value(clientId.toString()));
    }

    /**
     * Проверяет 404 для GET /external-requests/{id}/batches при отсутствии запроса.
     */
    @Test
    void should_returnNotFound_whenExternalRequestMissingForBatches() throws Exception {
        mockMvc.perform(get("/external-requests/{id}/batches", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXTERNAL_REQUEST_NOT_FOUND"));
    }

    /**
     * Проверяет GET /batch-requests/{id}/requests.
     */
    @Test
    void should_returnRequests_whenBatchRequestExists() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createExternalRequest(List.of(clientId));
        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();

        mockMvc.perform(get("/batch-requests/{id}/requests", batchRequestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].messageId").value(clientId.toString()));
    }

    /**
     * Проверяет 404 для GET /batch-requests/{id}/requests при отсутствии пачки.
     */
    @Test
    void should_returnNotFound_whenBatchRequestMissing() throws Exception {
        mockMvc.perform(get("/batch-requests/{id}/requests", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BATCH_REQUEST_NOT_FOUND"));
    }

    /**
     * Проверяет GET /clients/{id}/history после обработки ответа.
     */
    @Test
    void should_returnClientHistory_afterResponseProcessed() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createExternalRequest(List.of(clientId));
        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        UUID correlationId = externalRequest.getBatches().getFirst().getRequests().getFirst().getMessageId();

        outboxPublishProcessor.publishPendingMessages();
        externalResponseProcessor.process(
                ExternalResponseTestFixtures.updatedResponse(batchRequestId, correlationId));

        mockMvc.perform(get("/clients/{id}/history", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].externalRequestId").value(externalRequest.getId().toString()))
                .andExpect(jsonPath("$[0].outcome").value("UPDATED"));
    }

    /**
     * Проверяет 404 для GET /clients/{id}/history при отсутствии клиента.
     */
    @Test
    void should_returnNotFound_whenClientMissingForHistory() throws Exception {
        mockMvc.perform(get("/clients/{id}/history", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CLIENT_NOT_FOUND"));
    }

    /**
     * Создаёт внешний запрос через API.
     *
     * @param clientIds идентификаторы клиентов
     * @return агрегированный внешний запрос
     */
    private ExternalRequestTo createExternalRequest(List<UUID> clientIds) throws Exception {
        ExternalRequestBatchCreateRequest request = ExternalRequestTestFixtures.validBatchCreateRequest(clientIds);
        String responseJson = mockMvc.perform(post("/external-requests/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonMapper.readValue(responseJson, ExternalRequestTo.class);
    }
}
