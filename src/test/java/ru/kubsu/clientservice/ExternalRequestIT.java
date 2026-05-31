package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import ru.kubsu.clientservice.config.RabbitQueueProperties;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.repository.OutboxMessageRepository;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.ExternalRequestTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestManualCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestQueryParams;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.SourceType;
import ru.kubsu.contracts.exception.service.client.ExternalRequestNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты API внешних запросов.
 */
@TestPropertySource(properties = "app.batching.size=2")
class ExternalRequestIT extends IntegrationTestBase {

    /** Сервис персистентности клиентов для подготовки данных. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /** Репозиторий outbox-сообщений. */
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    /** Свойства имён RabbitMQ для проверки outbox-записей. */
    @Autowired
    private RabbitQueueProperties rabbitQueueProperties;

    /**
     * Проверяет batch-создание внешнего запроса и запись outbox на каждую пачку.
     */
    @Test
    void should_createBatchExternalRequest_whenValidClientIds() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestBatchCreateRequest request = ExternalRequestTestFixtures
                .validBatchCreateRequest(List.of(clientId));

        String responseJson = mockMvc.perform(post("/external-requests/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.batches.length()").value(1))
                .andExpect(jsonPath("$.batches[0].requests[0].clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.batches[0].requests[0].messageId").value(clientId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExternalRequestTo created = jsonMapper.readValue(responseJson, ExternalRequestTo.class);
        assertOutboxBatchEvent(created.getId(), SourceType.FNS, 1);
    }

    /**
     * Проверяет разбиение клиентов на несколько пачек по app.batching.size.
     */
    @Test
    void should_splitIntoMultipleBatches_whenClientCountExceedsBatchSize() throws Exception {
        List<UUID> clientIds = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            clientIds.add(clientPersistenceService.save(EntityTestFixtures.validClient()).getId());
        }

        ExternalRequestBatchCreateRequest request = ExternalRequestTestFixtures.validBatchCreateRequest(clientIds);

        mockMvc.perform(post("/external-requests/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batches.length()").value(2))
                .andExpect(jsonPath("$.batches[0].messageCount").value(2))
                .andExpect(jsonPath("$.batches[1].messageCount").value(1));
    }

    /**
     * Проверяет manual-создание внешнего запроса с generated correlationId.
     */
    @Test
    void should_createManualExternalRequest_whenValidPayload() throws Exception {
        ExternalRequestManualCreateRequest request = ExternalRequestTestFixtures.validManualCreateRequest();

        String responseJson = mockMvc.perform(post("/external-requests/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.batches.length()").value(1))
                .andExpect(jsonPath("$.batches[0].requests.length()").value(1))
                .andExpect(jsonPath("$.batches[0].requests[0].clientId").doesNotExist())
                .andExpect(jsonPath("$.batches[0].requests[0].type").value("MANUAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExternalRequestTo created = jsonMapper.readValue(responseJson, ExternalRequestTo.class);
        UUID correlationId = created.getBatches().getFirst().getRequests().getFirst().getMessageId();
        UUID clientId = created.getBatches().getFirst().getRequests().getFirst().getClientId();

        assertThat(clientId).isNull();
        assertThat(correlationId).isNotNull();
        assertOutboxBatchEvent(created.getId(), SourceType.EPGU, 1);
    }

    /**
     * Проверяет ответ 404 при запросе несуществующего внешнего запроса.
     */
    @Test
    void should_returnNotFound_whenExternalRequestMissing() throws Exception {
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(get("/external-requests/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ExternalRequestNotFoundException.ERROR_CODE));
    }

    /**
     * Проверяет поиск внешних запросов с кратким плоским ответом.
     */
    @Test
    void should_searchExternalRequests_withFlatListItem() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestBatchCreateRequest createRequest = ExternalRequestTestFixtures
                .validBatchCreateRequest(List.of(clientId));

        mockMvc.perform(post("/external-requests/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/external-requests/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ExternalRequestQueryParams()
                                .setSourceType(SourceType.FNS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].letterNumber").value("001"))
                .andExpect(jsonPath("$.content[0].batchCount").value(1))
                .andExpect(jsonPath("$.content[0].requestCount").value(1))
                .andExpect(jsonPath("$.content[0].batches").doesNotExist());
    }

    /**
     * Проверяет наличие outbox-событий EXTERNAL_REQUEST_SENT для внешнего запроса.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @param sourceType        тип внешнего источника
     * @param expectedCount     ожидаемое количество outbox-записей
     */
    private void assertOutboxBatchEvent(UUID externalRequestId, SourceType sourceType, int expectedCount) {
        List<OutboxMessage> messages = outboxMessageRepository.findAll().stream()
                .filter(message -> externalRequestId.equals(message.getAggregateId()))
                .filter(message -> OutboxEventType.EXTERNAL_REQUEST_SENT == message.getEventType())
                .toList();

        assertThat(messages).hasSize(expectedCount);
        assertThat(messages.getFirst().getExchangeName())
                .isEqualTo(rabbitQueueProperties.getExchange().getExternalRequest());
        assertThat(messages.getFirst().getRoutingKey())
                .isEqualTo(resolveExpectedRoutingKey(sourceType));
        assertThat(messages.getFirst().getPayload()).contains(externalRequestId.toString());
    }

    /**
     * Возвращает ожидаемый routing key для типа внешнего источника.
     *
     * @param sourceType тип внешнего источника
     * @return routing key из application.yml
     */
    private String resolveExpectedRoutingKey(SourceType sourceType) {
        return switch (sourceType) {
            case FNS -> rabbitQueueProperties.getRoutingKey().getExternalRequestFns();
            case EPGU -> rabbitQueueProperties.getRoutingKey().getExternalRequestEpgu();
        };
    }
}
