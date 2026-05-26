package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.BatchRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности пакетных запросов.
 */
public interface BatchRequestPersistenceService {

    /**
     * Сохраняет пакетный запрос в базе данных.
     *
     * @param batchRequest сущность пакетного запроса
     * @return сохранённый пакетный запрос
     */
    BatchRequest save(BatchRequest batchRequest);

    /**
     * Находит пакетный запрос по идентификатору.
     *
     * @param id идентификатор пакетного запроса
     * @return пакетный запрос, если найден
     */
    Optional<BatchRequest> findById(UUID id);
}
