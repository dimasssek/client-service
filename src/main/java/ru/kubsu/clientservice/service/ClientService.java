package ru.kubsu.clientservice.service;

import ru.kubsu.contracts.dto.service.client.ClientCreateRequest;
import ru.kubsu.contracts.dto.service.client.ClientQueryParams;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.dto.service.client.ClientUpdateRequest;
import ru.kubsu.contracts.dto.service.client.PageData;

import java.util.UUID;

/**
 * Сервис бизнес-логики работы с клиентами.
 */
public interface ClientService {

    /**
     * Создаёт нового клиента.
     *
     * @param request запрос создания
     * @return созданный клиент
     */
    ClientTo create(ClientCreateRequest request);

    /**
     * Возвращает клиента по идентификатору.
     *
     * @param id идентификатор клиента
     * @return клиент
     */
    ClientTo getById(UUID id);

    /**
     * Выполняет поиск клиентов по параметрам.
     *
     * @param params параметры поиска
     * @return страница клиентов
     */
    PageData<ClientTo> search(ClientQueryParams params);

    /**
     * Обновляет данные клиента.
     *
     * @param id      идентификатор клиента
     * @param request запрос обновления
     * @return обновлённый клиент
     */
    ClientTo update(UUID id, ClientUpdateRequest request);

    /**
     * Выполняет soft delete клиента.
     *
     * @param id идентификатор клиента
     */
    void delete(UUID id);
}
