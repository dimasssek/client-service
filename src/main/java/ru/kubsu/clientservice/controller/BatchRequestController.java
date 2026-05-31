package ru.kubsu.clientservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kubsu.clientservice.service.BatchRequestService;
import ru.kubsu.contracts.dto.service.client.RequestTo;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер чтения пакетных запросов.
 */
@RestController
@RequestMapping("/batch-requests")
@RequiredArgsConstructor
public class BatchRequestController {

    /** Сервис чтения пакетных запросов. */
    private final BatchRequestService batchRequestService;

    /**
     * Возвращает запросы пакетного запроса.
     *
     * @param id идентификатор пакетного запроса
     * @return список запросов
     */
    @GetMapping("/{id}/requests")
    public List<RequestTo> getRequests(@PathVariable UUID id) {
        return batchRequestService.getRequests(id);
    }
}
