package ru.kubsu.clientservice.service;

import ru.kubsu.contracts.dto.service.client.RequestTo;

import java.util.List;
import java.util.UUID;

/**
 * Сервис чтения пакетных запросов.
 */
public interface BatchRequestService {

    /**
     * Возвращает запросы пакетного запроса.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @return список запросов
     */
    List<RequestTo> getRequests(UUID batchRequestId);
}
