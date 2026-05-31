package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.kubsu.clientservice.config.OutboxProperties;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.outbox.OutboxPublishProcessor;
import ru.kubsu.clientservice.repository.ExternalRequestRepository;
import ru.kubsu.clientservice.repository.OutboxMessageRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.ClientTestFixtures;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.ExternalRequestTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;
import ru.kubsu.contracts.enums.service.client.RequestStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты outbox publisher при ошибках публикации.
 */
class OutboxPublisherFailureIT extends IntegrationTestBase {

    /** Обработчик публикации outbox-записей. */
    @Autowired
    private OutboxPublishProcessor outboxPublishProcessor;

    /** Репозиторий outbox-сообщений. */
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    /** Репозиторий запросов. */
    @Autowired
    private RequestRepository requestRepository;

    /** Репозиторий внешних запросов. */
    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    /** Свойства outbox publisher. */
    @Autowired
    private OutboxProperties outboxProperties;

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /** Заглушка RabbitTemplate для имитации ошибок брокера. */
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    /**
     * Проверяет увеличение attempts при ошибке публикации без перевода в FAILED.
     */
    @Test
    void should_incrementAttempts_whenPublishFails() throws Exception {
        UUID clientId = createClientOutboxMessage();
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        outboxPublishProcessor.publishPendingMessages();

        OutboxMessage message = findClientOutbox(clientId, OutboxEventType.CLIENT_CREATED);
        assertThat(message.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(message.getAttempts()).isEqualTo(1);
        assertThat(message.getLastError()).contains("RuntimeException: RabbitMQ unavailable");
        assertThat(message.getLastError()).contains("OutboxPublishProcessor.publishLockedMessage");
    }

    /**
     * Проверяет перевод outbox в FAILED и ERROR-статусы запроса после исчерпания попыток.
     */
    @Test
    void should_markOutboxAsFailed_andSetErrorStatuses_whenMaxAttemptsExceeded() throws Exception {
        ExternalRequestTo externalRequest = createExternalRequestViaApi();
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        for (int attempt = 0; attempt < outboxProperties.getMaxAttempts(); attempt++) {
            outboxPublishProcessor.publishPendingMessages();
        }

        OutboxMessage message = findExternalRequestOutbox(externalRequest.getId());
        assertThat(message.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(message.getAttempts()).isEqualTo(outboxProperties.getMaxAttempts());
        assertThat(message.getLastError()).isNotBlank();

        assertThat(externalRequestRepository.findById(externalRequest.getId()))
                .isPresent()
                .get()
                .extracting(er -> er.getStatus())
                .isEqualTo(RequestStatus.ERROR);

        List<Request> requests = requestRepository.findByBatchRequest_IdOrderByIdAsc(
                externalRequest.getBatches().getFirst().getId());
        assertThat(requests).isNotEmpty();
        assertThat(requests).allMatch(request -> request.getStatus() == RequestStatus.ERROR);
        assertThat(requests.getFirst().getErrorMessage()).contains("RuntimeException: RabbitMQ unavailable");
    }

    /**
     * Создаёт outbox-сообщение через API клиента.
     *
     * @return идентификатор созданного клиента
     */
    private UUID createClientOutboxMessage() throws Exception {
        String responseJson = mockMvc.perform(post("/clients")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(ClientTestFixtures.validCreateRequest())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonMapper.readValue(responseJson, ClientTo.class).getId();
    }

    /**
     * Создаёт внешний запрос через API и возвращает агрегированный ответ.
     *
     * @return созданный внешний запрос
     */
    private ExternalRequestTo createExternalRequestViaApi() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestBatchCreateRequest request = ExternalRequestTestFixtures
                .validBatchCreateRequest(List.of(clientId));

        String responseJson = mockMvc.perform(post("/external-requests/batch")
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonMapper.readValue(responseJson, ExternalRequestTo.class);
    }

    /**
     * Находит outbox-запись клиента по типу события.
     *
     * @param clientId  идентификатор клиента
     * @param eventType тип события
     * @return outbox-запись
     */
    private OutboxMessage findClientOutbox(UUID clientId, OutboxEventType eventType) {
        return outboxMessageRepository.findAll().stream()
                .filter(message -> clientId.equals(message.getAggregateId()))
                .filter(message -> eventType == message.getEventType())
                .findFirst()
                .orElseThrow();
    }

    /**
     * Находит outbox-запись внешнего запроса.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @return outbox-запись
     */
    private OutboxMessage findExternalRequestOutbox(UUID externalRequestId) {
        return outboxMessageRepository.findAll().stream()
                .filter(message -> externalRequestId.equals(message.getAggregateId()))
                .filter(message -> OutboxEventType.EXTERNAL_REQUEST_SENT == message.getEventType())
                .findFirst()
                .orElseThrow();
    }
}
