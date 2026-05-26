package ru.kubsu.clientservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.contracts.dto.service.client.ClientCreateRequest;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.dto.service.client.ClientUpdateRequest;

/**
 * MapStruct-маппер клиента.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper {

    /**
     * Преобразует сущность клиента в транспортный объект.
     *
     * @param client сущность клиента
     * @return транспортный объект
     */
    ClientTo toClientTo(Client client);

    /**
     * Преобразует запрос создания в сущность клиента.
     *
     * @param request запрос создания
     * @return сущность клиента
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actualDate", ignore = true)
    Client toEntity(ClientCreateRequest request);

    /**
     * Обновляет сущность клиента данными из запроса.
     *
     * @param request запрос обновления
     * @param client  сущность клиента
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actualDate", ignore = true)
    void updateEntity(ClientUpdateRequest request, @MappingTarget Client client);
}
