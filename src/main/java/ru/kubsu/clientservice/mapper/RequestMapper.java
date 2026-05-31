package ru.kubsu.clientservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.contracts.dto.service.client.ExternalRequestManualCreateRequest;
import ru.kubsu.contracts.dto.service.client.RequestTo;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.enums.service.client.RequestType;

import java.util.UUID;

/**
 * MapStruct-маппер запроса по клиенту.
 */
@Mapper(componentModel = "spring", imports = {RequestStatus.class, RequestType.class, UUID.class})
public interface RequestMapper {

    /**
     * Преобразует сущность запроса в транспортный объект.
     *
     * @param request сущность запроса
     * @return транспортный объект
     */
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "outcome", source = "outcome")
    RequestTo toRequestTo(Request request);

    /**
     * Создаёт запрос по клиенту из БД для batch-процесса.
     *
     * @param client       клиент
     * @param batchRequest пакетный запрос
     * @return новая сущность запроса
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", source = "client")
    @Mapping(target = "batchRequest", source = "batchRequest")
    @Mapping(target = "messageId", source = "client.id")
    @Mapping(target = "status", expression = "java(RequestStatus.PROCESSING)")
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "outcome", ignore = true)
    @Mapping(target = "type", expression = "java(RequestType.BATCH)")
    @Mapping(target = "firstName", source = "client.firstName")
    @Mapping(target = "lastName", source = "client.lastName")
    @Mapping(target = "patronymic", source = "client.patronymic")
    @Mapping(target = "birthDate", source = "client.birthDate")
    @Mapping(target = "gender", source = "client.gender")
    @Mapping(target = "identityDocumentSeries", source = "client.identityDocumentSeries")
    @Mapping(target = "identityDocumentNumber", source = "client.identityDocumentNumber")
    @Mapping(target = "identityDocumentIssueDate", source = "client.identityDocumentIssueDate")
    @Mapping(target = "itn", source = "client.itn")
    @Mapping(target = "insuranceNumber", source = "client.insuranceNumber")
    Request fromClient(Client client, BatchRequest batchRequest);

    /**
     * Создаёт ручной запрос без привязки к клиенту.
     *
     * @param createRequest запрос создания
     * @param batchRequest  пакетный запрос
     * @param correlationId идентификатор корреляции
     * @return новая сущность запроса
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "batchRequest", source = "batchRequest")
    @Mapping(target = "messageId", source = "correlationId")
    @Mapping(target = "status", expression = "java(RequestStatus.PROCESSING)")
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "outcome", ignore = true)
    @Mapping(target = "type", expression = "java(RequestType.MANUAL)")
    Request fromManual(ExternalRequestManualCreateRequest createRequest,
                       BatchRequest batchRequest,
                       UUID correlationId);
}
