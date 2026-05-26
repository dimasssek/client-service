package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.ExternalResponse;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.entity.Response;
import ru.kubsu.clientservice.entity.ResponseBatch;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.BatchRequestPersistenceService;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.service.ExternalRequestPersistenceService;
import ru.kubsu.clientservice.service.ExternalResponsePersistenceService;
import ru.kubsu.clientservice.service.OutboxMessagePersistenceService;
import ru.kubsu.clientservice.service.RequestPersistenceService;
import ru.kubsu.clientservice.service.ResponseBatchPersistenceService;
import ru.kubsu.clientservice.service.ResponsePersistenceService;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.enums.service.client.AggregateType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Интеграционные тесты персистентности доменных сущностей.
 */
class EntityPersistenceIT extends IntegrationTestBase {

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /** Сервис персистентности внешних запросов. */
    @Autowired
    private ExternalRequestPersistenceService externalRequestPersistenceService;

    /** Сервис персистентности пакетных запросов. */
    @Autowired
    private BatchRequestPersistenceService batchRequestPersistenceService;

    /** Сервис персистентности запросов. */
    @Autowired
    private RequestPersistenceService requestPersistenceService;

    /** Сервис персистентности пачек ответов. */
    @Autowired
    private ResponseBatchPersistenceService responseBatchPersistenceService;

    /** Сервис персистентности внешних ответов. */
    @Autowired
    private ExternalResponsePersistenceService externalResponsePersistenceService;

    /** Сервис персистентности данных клиента из ответа. */
    @Autowired
    private ResponsePersistenceService responsePersistenceService;

    /** Сервис персистентности outbox-сообщений. */
    @Autowired
    private OutboxMessagePersistenceService outboxMessagePersistenceService;

    /** Репозиторий клиентов для негативных сценариев. */
    @Autowired
    private ClientRepository clientRepository;

    /** Репозиторий запросов для негативных сценариев. */
    @Autowired
    private RequestRepository requestRepository;

    /**
     * Проверяет сохранение и загрузку клиента.
     */
    @Test
    void should_persistAndLoad_client() {
        Client saved = clientPersistenceService.save(EntityTestFixtures.validClient());

        assertThat(saved.getId()).isNotNull();
        assertThat(clientPersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Client::getLastName)
                .isEqualTo("Иванов");
    }

    /**
     * Проверяет сохранение и загрузку внешнего запроса.
     */
    @Test
    void should_persistAndLoad_externalRequest() {
        ExternalRequest saved = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());

        assertThat(saved.getId()).isNotNull();
        assertThat(externalRequestPersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(ExternalRequest::getLetterNumber)
                .isEqualTo("001");
    }

    /**
     * Проверяет сохранение и загрузку пакетного запроса со связью на внешний запрос.
     */
    @Test
    void should_persistAndLoad_batchRequest() {
        ExternalRequest externalRequest = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());
        BatchRequest saved = batchRequestPersistenceService.save(EntityTestFixtures.validBatchRequest(externalRequest));

        assertThat(saved.getId()).isNotNull();
        assertThat(batchRequestPersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(batch -> batch.getExternalRequest().getId())
                .isEqualTo(externalRequest.getId());
    }

    /**
     * Проверяет сохранение и загрузку запроса со связями на клиента и пакет.
     */
    @Test
    void should_persistAndLoad_request() {
        Client client = clientPersistenceService.save(EntityTestFixtures.validClient());
        ExternalRequest externalRequest = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());
        BatchRequest batchRequest = batchRequestPersistenceService.save(EntityTestFixtures.validBatchRequest(externalRequest));
        Request saved = requestPersistenceService.save(EntityTestFixtures.validRequest(client, batchRequest));

        assertThat(saved.getId()).isNotNull();
        assertThat(requestPersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .satisfies(request -> {
                    assertThat(request.getClient().getId()).isEqualTo(client.getId());
                    assertThat(request.getBatchRequest().getId()).isEqualTo(batchRequest.getId());
                });
    }

    /**
     * Проверяет сохранение и загрузку пачки ответа.
     */
    @Test
    void should_persistAndLoad_responseBatch() {
        ExternalRequest externalRequest = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());
        BatchRequest batchRequest = batchRequestPersistenceService.save(EntityTestFixtures.validBatchRequest(externalRequest));
        ResponseBatch saved = responseBatchPersistenceService.save(EntityTestFixtures.validResponseBatch(batchRequest));

        assertThat(saved.getId()).isNotNull();
        assertThat(responseBatchPersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(batch -> batch.getBatchRequest().getId())
                .isEqualTo(batchRequest.getId());
    }

    /**
     * Проверяет сохранение и загрузку внешнего ответа.
     */
    @Test
    void should_persistAndLoad_externalResponse() {
        Client client = clientPersistenceService.save(EntityTestFixtures.validClient());
        ExternalRequest externalRequest = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());
        BatchRequest batchRequest = batchRequestPersistenceService.save(EntityTestFixtures.validBatchRequest(externalRequest));
        Request request = requestPersistenceService.save(EntityTestFixtures.validRequest(client, batchRequest));
        ResponseBatch responseBatch = responseBatchPersistenceService.save(EntityTestFixtures.validResponseBatch(batchRequest));
        ExternalResponse saved = externalResponsePersistenceService.save(
                EntityTestFixtures.validExternalResponse(responseBatch, request, client));

        assertThat(saved.getId()).isNotNull();
        assertThat(externalResponsePersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(ExternalResponse::getCorrelationId)
                .isEqualTo(request.getMessageId());
    }

    /**
     * Проверяет сохранение и загрузку данных клиента из ответа.
     */
    @Test
    void should_persistAndLoad_response() {
        Client client = clientPersistenceService.save(EntityTestFixtures.validClient());
        ExternalRequest externalRequest = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());
        BatchRequest batchRequest = batchRequestPersistenceService.save(EntityTestFixtures.validBatchRequest(externalRequest));
        Request request = requestPersistenceService.save(EntityTestFixtures.validRequest(client, batchRequest));
        ResponseBatch responseBatch = responseBatchPersistenceService.save(EntityTestFixtures.validResponseBatch(batchRequest));
        ExternalResponse externalResponse = externalResponsePersistenceService.save(
                EntityTestFixtures.validExternalResponse(responseBatch, request, client));
        Response saved = responsePersistenceService.save(EntityTestFixtures.validResponse(externalResponse));

        assertThat(saved.getId()).isNotNull();
        assertThat(responsePersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Response::getLastName)
                .isEqualTo("Иванов");
    }

    /**
     * Проверяет сохранение и загрузку outbox-сообщения с JSON payload.
     */
    @Test
    void should_persistAndLoad_outboxMessage() {
        Client client = clientPersistenceService.save(EntityTestFixtures.validClient());
        OutboxMessage saved = outboxMessagePersistenceService.save(EntityTestFixtures.validOutboxMessage(client.getId()));

        assertThat(saved.getId()).isNotNull();
        assertThat(outboxMessagePersistenceService.findById(saved.getId()))
                .isPresent()
                .get()
                .satisfies(message -> {
                    assertThat(message.getPayload()).contains(client.getId().toString());
                    assertThat(message.getAggregateType()).isEqualTo(AggregateType.CLIENT);
                });
    }

    /**
     * Проверяет, что при сохранении запроса с несуществующим client_id нарушается FK.
     */
    @Test
    void should_throwDataIntegrityViolation_whenSavingRequestWithUnknownClientId() {
        ExternalRequest externalRequest = externalRequestPersistenceService.save(EntityTestFixtures.validExternalRequest());
        BatchRequest batchRequest = batchRequestPersistenceService.save(EntityTestFixtures.validBatchRequest(externalRequest));
        Client unknownClient = new Client().setId(UUID.randomUUID());

        Request request = EntityTestFixtures.validRequest(unknownClient, batchRequest);

        // FK на client должен отклонить сохранение запроса с несуществующим client_id
        assertThatThrownBy(() -> requestRepository.saveAndFlush(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Проверяет, что NOT NULL ограничение на last_name клиента не позволяет сохранить запись.
     */
    @Test
    void should_throwDataIntegrityViolation_whenSavingClientWithoutRequiredField() {
        Client invalidClient = EntityTestFixtures.validClient().setLastName(null);

        // last_name обязателен на уровне БД
        assertThatThrownBy(() -> clientRepository.saveAndFlush(invalidClient))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * Проверяет, что репозиторий возвращает пустой Optional для несуществующего клиента.
     */
    @Test
    void should_returnEmpty_whenLoadingMissingClientById() {
        assertTrue(clientRepository.findById(UUID.randomUUID()).isEmpty());
    }
}
