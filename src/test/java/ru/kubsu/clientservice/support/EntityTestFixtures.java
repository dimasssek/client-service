package ru.kubsu.clientservice.support;

import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.ExternalResponse;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.entity.Response;
import ru.kubsu.clientservice.entity.ResponseBatch;
import ru.kubsu.contracts.enums.service.client.AggregateType;
import ru.kubsu.contracts.enums.service.client.ClientStatus;
import ru.kubsu.contracts.enums.service.client.Gender;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.enums.service.client.RequestType;
import ru.kubsu.contracts.enums.service.client.SourceType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Фабрика тестовых сущностей для интеграционных тестов.
 */
public final class EntityTestFixtures {

    private EntityTestFixtures() {
    }

    /**
     * Создаёт валидного клиента для сохранения в БД.
     *
     * @return новая сущность клиента
     */
    public static Client validClient() {
        return new Client()
                .setLastName("Иванов")
                .setFirstName("Иван")
                .setPatronymic("Иванович")
                .setBirthDate(LocalDate.of(1990, 1, 15))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries("1234")
                .setIdentityDocumentNumber("567890")
                .setIdentityDocumentIssueDate(LocalDate.of(2010, 5, 20))
                .setItn("123456789012")
                .setInsuranceNumber("123-456-789 00")
                .setAddressDefined(true)
                .setResidenceAddressName("г. Москва, ул. Примерная, д. 1")
                .setStatus(ClientStatus.ACTUAL);
    }

    /**
     * Создаёт валидный внешний запрос для сохранения в БД.
     *
     * @return новая сущность внешнего запроса
     */
    public static ExternalRequest validExternalRequest() {
        return new ExternalRequest()
                .setLetterNumber("001")
                .setLetterDate(LocalDate.of(2026, 1, 10))
                .setSourceType(SourceType.FNS)
                .setStatus(RequestStatus.PROCESSING)
                .setInitiatorLogin("operator")
                .setCreated(OffsetDateTime.now());
    }

    /**
     * Создаёт валидный пакетный запрос для сохранения в БД.
     *
     * @param externalRequest связанный внешний запрос
     * @return новая сущность пакетного запроса
     */
    public static BatchRequest validBatchRequest(ExternalRequest externalRequest) {
        return new BatchRequest()
                .setMessageId(UUID.randomUUID())
                .setCreatedDate(OffsetDateTime.now())
                .setExternalRequest(externalRequest)
                .setMessageCount(1);
    }

    /**
     * Создаёт валидный запрос по клиенту для сохранения в БД.
     *
     * @param client        связанный клиент
     * @param batchRequest  связанный пакетный запрос
     * @return новая сущность запроса
     */
    public static Request validRequest(Client client, BatchRequest batchRequest) {
        return new Request()
                .setClient(client)
                .setMessageId(UUID.randomUUID())
                .setBatchRequest(batchRequest)
                .setStatus(RequestStatus.PROCESSING)
                .setFirstName(client.getFirstName())
                .setLastName(client.getLastName())
                .setPatronymic(client.getPatronymic())
                .setBirthDate(client.getBirthDate())
                .setGender(client.getGender())
                .setIdentityDocumentSeries(client.getIdentityDocumentSeries())
                .setIdentityDocumentNumber(client.getIdentityDocumentNumber())
                .setType(RequestType.BATCH);
    }

    /**
     * Создаёт валидную пачку ответа для сохранения в БД.
     *
     * @param batchRequest связанный пакетный запрос
     * @return новая сущность пачки ответа
     */
    public static ResponseBatch validResponseBatch(BatchRequest batchRequest) {
        return new ResponseBatch()
                .setBatchRequest(batchRequest)
                .setSourceType(SourceType.FNS)
                .setMessageId(UUID.randomUUID())
                .setReceivedDate(OffsetDateTime.now());
    }

    /**
     * Создаёт валидный внешний ответ для сохранения в БД.
     *
     * @param responseBatch связанная пачка ответа
     * @param request       связанный запрос
     * @param client        связанный клиент
     * @return новая сущность внешнего ответа
     */
    public static ExternalResponse validExternalResponse(ResponseBatch responseBatch,
                                                         Request request,
                                                         Client client) {
        return new ExternalResponse()
                .setResponseBatch(responseBatch)
                .setRequest(request)
                .setCorrelationId(request.getMessageId())
                .setClient(client)
                .setStatus(RequestStatus.RECEIVED);
    }

    /**
     * Создаёт валидные данные клиента из ответа для сохранения в БД.
     *
     * @param externalResponse связанный внешний ответ
     * @return новая сущность данных клиента из ответа
     */
    public static Response validResponse(ExternalResponse externalResponse) {
        return new Response()
                .setExternalResponse(externalResponse)
                .setFirstName("Иван")
                .setLastName("Иванов")
                .setPatronymic("Иванович")
                .setBirthDate(LocalDate.of(1990, 1, 15))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries("1234")
                .setIdentityDocumentNumber("567890")
                .setIdentityDocumentIssueDate(LocalDate.of(2010, 5, 20))
                .setResidenceAddressName("г. Москва, ул. Примерная, д. 1");
    }

    /**
     * Создаёт валидное outbox-сообщение для сохранения в БД.
     *
     * @param aggregateId идентификатор агрегата
     * @return новая сущность outbox-сообщения
     */
    public static OutboxMessage validOutboxMessage(UUID aggregateId) {
        return new OutboxMessage()
                .setAggregateType(AggregateType.CLIENT)
                .setAggregateId(aggregateId)
                .setEventType(OutboxEventType.CLIENT_CREATED)
                .setPayload("{\"clientId\":\"" + aggregateId + "\"}")
                .setExchangeName("client.events.exchange")
                .setRoutingKey("client.created")
                .setStatus(OutboxStatus.NEW)
                .setAttempts(0)
                .setCreatedAt(OffsetDateTime.now());
    }
}
