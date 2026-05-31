package ru.kubsu.clientservice.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.kubsu.clientservice.config.RabbitQueueProperties;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.mapper.ClientMapper;
import ru.kubsu.clientservice.mapper.ExternalRequestMapper;
import ru.kubsu.clientservice.service.OutboxMessagePersistenceService;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.enums.service.client.AggregateType;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;
import ru.kubsu.contracts.enums.service.client.SourceType;
import ru.kubsu.contracts.messaging.service.client.ExternalRequestBatchMessage;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Запись outbox-сообщений в рамках бизнес-транзакции.
 */
@Component
@RequiredArgsConstructor
public class OutboxWriter {

    /** Сервис персистентности outbox-сообщений. */
    private final OutboxMessagePersistenceService outboxMessagePersistenceService;

    /** Свойства имён RabbitMQ. */
    private final RabbitQueueProperties rabbitQueueProperties;

    /** MapStruct-маппер клиента. */
    private final ClientMapper clientMapper;

    /** MapStruct-маппер внешних запросов. */
    private final ExternalRequestMapper externalRequestMapper;

    /** JSON-сериализатор. */
    private final JsonMapper jsonMapper;

    /**
     * Сохраняет outbox-событие по клиенту.
     *
     * @param client    клиент
     * @param eventType тип события
     */
    public void writeClientEvent(Client client, OutboxEventType eventType) {
        ClientTo payload = clientMapper.toClientTo(client);
        String routingKey = resolveRoutingKey(eventType);

        OutboxMessage outboxMessage = new OutboxMessage()
                .setAggregateType(AggregateType.CLIENT)
                .setAggregateId(client.getId())
                .setEventType(eventType)
                .setPayload(jsonMapper.writeValueAsString(payload))
                .setExchangeName(rabbitQueueProperties.getExchange().getClientEvents())
                .setRoutingKey(routingKey)
                .setStatus(OutboxStatus.NEW)
                .setAttempts(0)
                .setCreatedAt(OffsetDateTime.now());

        outboxMessagePersistenceService.save(outboxMessage);
    }

    /**
     * Сохраняет outbox-событие отправки пакетного запроса во внешний источник.
     *
     * @param externalRequest внешний запрос
     * @param batchRequest    пакетный запрос
     * @param requests        запросы внутри пачки
     */
    public void writeExternalRequestBatchEvent(ExternalRequest externalRequest,
                                               BatchRequest batchRequest,
                                               List<Request> requests) {
        ExternalRequestBatchMessage payload = externalRequestMapper.toBatchMessage(
                externalRequest, batchRequest, requests);

        OutboxMessage outboxMessage = new OutboxMessage()
                .setAggregateType(AggregateType.EXTERNAL_REQUEST)
                .setAggregateId(externalRequest.getId())
                .setEventType(OutboxEventType.EXTERNAL_REQUEST_SENT)
                .setPayload(jsonMapper.writeValueAsString(payload))
                .setExchangeName(rabbitQueueProperties.getExchange().getExternalRequest())
                .setRoutingKey(resolveExternalRequestRoutingKey(externalRequest.getSourceType()))
                .setStatus(OutboxStatus.NEW)
                .setAttempts(0)
                .setCreatedAt(OffsetDateTime.now());

        outboxMessagePersistenceService.save(outboxMessage);
    }

    /**
     * Определяет routing key по типу события.
     *
     * @param eventType тип события
     * @return routing key
     */
    private String resolveRoutingKey(OutboxEventType eventType) {
        return switch (eventType) {
            case CLIENT_CREATED -> rabbitQueueProperties.getRoutingKey().getClientCreated();
            case CLIENT_UPDATED -> rabbitQueueProperties.getRoutingKey().getClientUpdated();
            case CLIENT_DELETED -> rabbitQueueProperties.getRoutingKey().getClientDeleted();
            default -> throw new IllegalArgumentException("Неподдерживаемый тип outbox-события: " + eventType);
        };
    }

    /**
     * Определяет routing key исходящего запроса по типу внешнего источника.
     *
     * @param sourceType тип внешнего источника
     * @return routing key
     */
    private String resolveExternalRequestRoutingKey(SourceType sourceType) {
        return switch (sourceType) {
            case FNS -> rabbitQueueProperties.getRoutingKey().getExternalRequestFns();
            case EPGU -> rabbitQueueProperties.getRoutingKey().getExternalRequestEpgu();
        };
    }
}
