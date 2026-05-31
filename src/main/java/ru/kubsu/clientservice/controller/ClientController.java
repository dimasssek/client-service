package ru.kubsu.clientservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.clientservice.service.ClientService;
import ru.kubsu.contracts.dto.service.client.ClientCreateRequest;
import ru.kubsu.contracts.dto.service.client.ClientHistoryEntryTo;
import ru.kubsu.contracts.dto.service.client.ClientQueryParams;
import ru.kubsu.contracts.dto.service.client.ClientTo;
import ru.kubsu.contracts.dto.service.client.ClientUpdateRequest;
import ru.kubsu.contracts.dto.service.client.PageData;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер работы с клиентами.
 */
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    /** Сервис бизнес-логики клиентов. */
    private final ClientService clientService;

    /**
     * Создаёт нового клиента.
     *
     * @param request запрос создания
     * @return созданный клиент
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientTo create(@Valid @RequestBody ClientCreateRequest request) {
        return clientService.create(request);
    }

    /**
     * Возвращает клиента по идентификатору.
     *
     * @param id идентификатор клиента
     * @return клиент
     */
    @GetMapping("/{id}")
    public ClientTo getById(@PathVariable UUID id) {
        return clientService.getById(id);
    }

    /**
     * Возвращает историю запросов клиента во внешние ведомства.
     *
     * @param id идентификатор клиента
     * @return список записей истории
     */
    @GetMapping("/{id}/history")
    public List<ClientHistoryEntryTo> getHistory(@PathVariable UUID id) {
        return clientService.getHistory(id);
    }

    /**
     * Выполняет поиск клиентов по параметрам.
     *
     * @param params параметры поиска
     * @return страница клиентов
     */
    @PostMapping("/search")
    public PageData<ClientTo> search(@Valid @RequestBody ClientQueryParams params) {
        return clientService.search(params);
    }

    /**
     * Обновляет данные клиента.
     *
     * @param id      идентификатор клиента
     * @param request запрос обновления
     * @return обновлённый клиент
     */
    @PutMapping("/{id}")
    public ClientTo update(@PathVariable UUID id, @Valid @RequestBody ClientUpdateRequest request) {
        return clientService.update(id, request);
    }

    /**
     * Выполняет soft delete клиента.
     *
     * @param id идентификатор клиента
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        clientService.delete(id);
    }
}
