package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.Request;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности запросов по клиентам.
 */
public interface RequestPersistenceService {

    /**
     * Сохраняет запрос в базе данных.
     *
     * @param request сущность запроса
     * @return сохранённый запрос
     */
    Request save(Request request);

    /**
     * Находит запрос по идентификатору.
     *
     * @param id идентификатор запроса
     * @return запрос, если найден
     */
    Optional<Request> findById(UUID id);
}
