package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.ResponseBatch;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности пачек ответов.
 */
public interface ResponseBatchPersistenceService {

    /**
     * Сохраняет пачку ответа в базе данных.
     *
     * @param responseBatch сущность пачки ответа
     * @return сохранённая пачка ответа
     */
    ResponseBatch save(ResponseBatch responseBatch);

    /**
     * Находит пачку ответа по идентификатору.
     *
     * @param id идентификатор пачки ответа
     * @return пачка ответа, если найдена
     */
    Optional<ResponseBatch> findById(UUID id);
}
