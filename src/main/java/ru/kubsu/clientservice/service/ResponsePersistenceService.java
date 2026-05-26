package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.Response;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности данных клиента из ответа.
 */
public interface ResponsePersistenceService {

    /**
     * Сохраняет данные клиента из ответа в базе данных.
     *
     * @param response сущность ответа
     * @return сохранённые данные
     */
    Response save(Response response);

    /**
     * Находит данные клиента из ответа по идентификатору.
     *
     * @param id идентификатор записи
     * @return данные, если найдены
     */
    Optional<Response> findById(UUID id);
}
