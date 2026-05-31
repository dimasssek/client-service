package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import ru.kubsu.clientservice.entity.OutboxMessage;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты outbox publisher.
 */
@TestPropertySource(properties = "app.batching.size=2")
class OutboxPublisherIT extends IntegrationTestBase {

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

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /**
     * Проверяет публикацию outbox-события клиента и перевод записи в SENT.
     */
    @Test
    void should_publishClientOutboxMessageToRabbit() throws Exception {
        String responseJson = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(ClientTestFixtures.validCreateRequest())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ClientTo created = jsonMapper.readValue(responseJson, ClientTo.class);

        outboxPublishProcessor.publishPendingMessages();

        OutboxMessage message = findClientOutbox(created.getId(), OutboxEventType.CLIENT_CREATED);
        assertThat(message.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(message.getSentAt()).isNotNull();
        assertThat(message.getLastError()).isNull();
    }

    /**
     * Проверяет перевод Request в SENT после публикации outbox пачки.
     */
    @Test
    void should_updateRequestStatusToSent_afterExternalBatchPublished() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createExternalRequest(List.of(clientId));

        outboxPublishProcessor.publishPendingMessages();

        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();
        assertThat(requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId))
                .allMatch(request -> request.getStatus() == RequestStatus.SENT);

        assertThat(externalRequestRepository.findById(externalRequest.getId()))
                .isPresent()
                .get()
                .extracting(er -> er.getStatus())
                .isEqualTo(RequestStatus.SENT);
    }

    /**
     * Проверяет перевод ExternalRequest в SENT только после отправки всех пачек.
     */
    @Test
    void should_updateExternalRequestToSent_whenAllBatchesPublished() throws Exception {
        List<UUID> clientIds = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            clientIds.add(clientPersistenceService.save(EntityTestFixtures.validClient()).getId());
        }

        ExternalRequestTo externalRequest = createExternalRequest(clientIds);
        assertThat(externalRequest.getBatches()).hasSize(2);
        assertThat(outboxMessageRepository.findAll().stream()
                .filter(message -> externalRequest.getId().equals(message.getAggregateId()))
                .count()).isEqualTo(2);

        outboxPublishProcessor.publishPendingMessages();

        assertThat(outboxMessageRepository.findAll().stream()
                .filter(message -> externalRequest.getId().equals(message.getAggregateId()))
                .map(OutboxMessage::getStatus))
                .containsOnly(OutboxStatus.SENT);

        assertThat(externalRequestRepository.findById(externalRequest.getId()))
                .isPresent()
                .get()
                .extracting(er -> er.getStatus())
                .isEqualTo(RequestStatus.SENT);
    }

    /**
     * Проверяет, что outbox-запись не публикуется повторно после успешной отправки.
     */
    @Test
    void should_notRepublishAlreadySentOutboxMessage() throws Exception {
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(ClientTestFixtures.validCreateRequest())))
                .andExpect(status().isCreated());

        outboxPublishProcessor.publishPendingMessages();
        long sentCount = outboxMessageRepository.findAll().stream()
                .filter(message -> message.getStatus() == OutboxStatus.SENT)
                .count();

        outboxPublishProcessor.publishPendingMessages();

        assertThat(outboxMessageRepository.findAll().stream()
                .filter(message -> message.getStatus() == OutboxStatus.SENT)
                .count()).isEqualTo(sentCount);
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
}
