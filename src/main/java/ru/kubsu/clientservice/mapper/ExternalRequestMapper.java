package ru.kubsu.clientservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.contracts.dto.service.client.BatchRequestTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestListTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.messaging.service.client.ExternalRequestBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalRequestItemMessage;

import java.util.List;

/**
 * MapStruct-маппер внешних запросов и связанных сущностей.
 */
@Mapper(componentModel = "spring", uses = RequestMapper.class)
public interface ExternalRequestMapper {

    /**
     * Преобразует сущность внешнего запроса в агрегированный транспортный объект.
     *
     * @param externalRequest сущность внешнего запроса
     * @return агрегированный транспортный объект
     */
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "summary", ignore = true)
    ExternalRequestTo toExternalRequestTo(ExternalRequest externalRequest);

    /**
     * Преобразует сущность внешнего запроса в краткий транспортный объект.
     *
     * @param externalRequest сущность внешнего запроса
     * @return краткий транспортный объект
     */
    ExternalRequestListTo toExternalRequestListTo(ExternalRequest externalRequest);

    /**
     * Преобразует пакетный запрос в транспортный объект.
     *
     * @param batchRequest сущность пакетного запроса
     * @return транспортный объект
     */
    @Mapping(target = "requests", ignore = true)
    BatchRequestTo toBatchRequestTo(BatchRequest batchRequest);

    /**
     * Преобразует пакетный запрос и его запросы в сообщение outbox.
     *
     * @param externalRequest внешний запрос
     * @param batchRequest    пакетный запрос
     * @param requests        запросы внутри пачки
     * @return сообщение для outbox
     */
    @Mapping(target = "batchRequestId", source = "batchRequest.id")
    @Mapping(target = "externalRequestId", source = "externalRequest.id")
    @Mapping(target = "batchMessageId", source = "batchRequest.messageId")
    @Mapping(target = "sourceType", source = "externalRequest.sourceType")
    @Mapping(target = "letterNumber", source = "externalRequest.letterNumber")
    @Mapping(target = "letterDate", source = "externalRequest.letterDate")
    @Mapping(target = "items", source = "requests")
    ExternalRequestBatchMessage toBatchMessage(ExternalRequest externalRequest,
                                               BatchRequest batchRequest,
                                               List<Request> requests);

    /**
     * Преобразует запрос в элемент сообщения outbox.
     *
     * @param request запрос
     * @return элемент сообщения
     */
    @Mapping(target = "requestId", source = "id")
    @Mapping(target = "correlationId", source = "messageId")
    @Mapping(target = "clientId", source = "client.id")
    ExternalRequestItemMessage toItemMessage(Request request);
}
