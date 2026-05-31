package ru.kubsu.clientservice.service;

import ru.kubsu.contracts.dto.service.client.BatchRequestTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestListTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestManualCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestQueryParams;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.dto.common.PageData;

import java.util.List;
import java.util.UUID;

/**
 * Сервис бизнес-логики работы с внешними запросами.
 */
public interface ExternalRequestService {

    /**
     * Создаёт внешний запрос по списку клиентов из БД.
     *
     * @param request запрос создания batch-процесса
     * @return агрегированный внешний запрос
     */
    ExternalRequestTo createBatch(ExternalRequestBatchCreateRequest request);

    /**
     * Создаёт внешний запрос с ручным вводом данных клиента.
     *
     * @param request запрос создания manual-процесса
     * @return агрегированный внешний запрос
     */
    ExternalRequestTo createManual(ExternalRequestManualCreateRequest request);

    /**
     * Возвращает агрегированный внешний запрос по идентификатору.
     *
     * @param id идентификатор внешнего запроса
     * @return агрегированный внешний запрос
     */
    ExternalRequestTo getById(UUID id);

    /**
     * Возвращает пакетные запросы внешнего запроса.
     *
     * @param id идентификатор внешнего запроса
     * @return список пакетных запросов с запросами по клиентам
     */
    List<BatchRequestTo> getBatches(UUID id);

    /**
     * Выполняет поиск внешних запросов по параметрам.
     *
     * @param params параметры поиска
     * @return страница кратких записей
     */
    PageData<ExternalRequestListTo> search(ExternalRequestQueryParams params);
}
