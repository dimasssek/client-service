package ru.kubsu.clientservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.contracts.dto.service.client.ClientHistoryEntryTo;

/**
 * MapStruct-маппер истории запросов клиента.
 */
@Mapper(componentModel = "spring")
public interface ClientHistoryMapper {

    /**
     * Формирует запись истории по запросу и внешнему запросу.
     *
     * @param request         запрос по клиенту
     * @param externalRequest внешний запрос
     * @return запись истории
     */
    @Mapping(target = "externalRequestId", source = "externalRequest.id")
    @Mapping(target = "letterNumber", source = "externalRequest.letterNumber")
    @Mapping(target = "sourceType", source = "externalRequest.sourceType")
    @Mapping(target = "externalRequestStatus", source = "externalRequest.status")
    @Mapping(target = "externalRequestCreated", source = "externalRequest.created")
    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "requestStatus", source = "request.status")
    @Mapping(target = "outcome", source = "request.outcome")
    ClientHistoryEntryTo toHistoryEntry(Request request, ExternalRequest externalRequest);
}
