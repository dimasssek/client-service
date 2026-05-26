package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.ExternalRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности внешних запросов.
 */
public interface ExternalRequestPersistenceService {

    /**
     * Сохраняет внешний запрос в базе данных.
     *
     * @param externalRequest сущность внешнего запроса
     * @return сохранённый внешний запрос
     */
    ExternalRequest save(ExternalRequest externalRequest);

    /**
     * Находит внешний запрос по идентификатору.
     *
     * @param id идентификатор внешнего запроса
     * @return внешний запрос, если найден
     */
    Optional<ExternalRequest> findById(UUID id);
}
