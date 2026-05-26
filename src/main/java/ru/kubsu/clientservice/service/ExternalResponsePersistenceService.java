package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.ExternalResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности внешних ответов.
 */
public interface ExternalResponsePersistenceService {

    /**
     * Сохраняет внешний ответ в базе данных.
     *
     * @param externalResponse сущность внешнего ответа
     * @return сохранённый внешний ответ
     */
    ExternalResponse save(ExternalResponse externalResponse);

    /**
     * Находит внешний ответ по идентификатору.
     *
     * @param id идентификатор внешнего ответа
     * @return внешний ответ, если найден
     */
    Optional<ExternalResponse> findById(UUID id);
}
