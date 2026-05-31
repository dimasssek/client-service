package ru.kubsu.clientservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.clientservice.service.ExternalRequestService;
import ru.kubsu.contracts.dto.service.client.BatchRequestTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestListTo;
import ru.kubsu.contracts.dto.service.client.ExternalRequestManualCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestQueryParams;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.dto.service.client.PageData;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер работы с внешними запросами.
 */
@RestController
@RequestMapping("/external-requests")
@RequiredArgsConstructor
public class ExternalRequestController {

    /** Сервис бизнес-логики внешних запросов. */
    private final ExternalRequestService externalRequestService;

    /**
     * Создаёт внешний запрос по списку клиентов из БД.
     *
     * @param request запрос создания batch-процесса
     * @return агрегированный внешний запрос
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalRequestTo createBatch(@Valid @RequestBody ExternalRequestBatchCreateRequest request) {
        return externalRequestService.createBatch(request);
    }

    /**
     * Создаёт внешний запрос с ручным вводом данных клиента.
     *
     * @param request запрос создания manual-процесса
     * @return агрегированный внешний запрос
     */
    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalRequestTo createManual(@Valid @RequestBody ExternalRequestManualCreateRequest request) {
        return externalRequestService.createManual(request);
    }

    /**
     * Возвращает агрегированный внешний запрос по идентификатору.
     *
     * @param id идентификатор внешнего запроса
     * @return агрегированный внешний запрос
     */
    @GetMapping("/{id}")
    public ExternalRequestTo getById(@PathVariable UUID id) {
        return externalRequestService.getById(id);
    }

    /**
     * Возвращает пакетные запросы внешнего запроса.
     *
     * @param id идентификатор внешнего запроса
     * @return список пакетных запросов
     */
    @GetMapping("/{id}/batches")
    public List<BatchRequestTo> getBatches(@PathVariable UUID id) {
        return externalRequestService.getBatches(id);
    }

    /**
     * Выполняет поиск внешних запросов по параметрам.
     *
     * @param params параметры поиска
     * @return страница кратких записей
     */
    @PostMapping("/search")
    public PageData<ExternalRequestListTo> search(@Valid @RequestBody ExternalRequestQueryParams params) {
        return externalRequestService.search(params);
    }
}
