package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.OutboxMessage;

import java.util.UUID;

/**
 * Репозиторий outbox-сообщений.
 */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {
}
