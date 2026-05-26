package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.OutboxMessage;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности outbox-сообщений.
 */
public interface OutboxMessagePersistenceService {

    /**
     * Сохраняет outbox-сообщение в базе данных.
     *
     * @param outboxMessage сущность outbox-сообщения
     * @return сохранённое outbox-сообщение
     */
    OutboxMessage save(OutboxMessage outboxMessage);

    /**
     * Находит outbox-сообщение по идентификатору.
     *
     * @param id идентификатор outbox-сообщения
     * @return outbox-сообщение, если найдено
     */
    Optional<OutboxMessage> findById(UUID id);
}
