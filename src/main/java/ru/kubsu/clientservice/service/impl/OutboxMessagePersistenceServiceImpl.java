package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.repository.OutboxMessageRepository;
import ru.kubsu.clientservice.service.OutboxMessagePersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности outbox-сообщений.
 */
@Service
@RequiredArgsConstructor
public class OutboxMessagePersistenceServiceImpl implements OutboxMessagePersistenceService {

    /** Репозиторий outbox-сообщений. */
    private final OutboxMessageRepository outboxMessageRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OutboxMessage save(OutboxMessage outboxMessage) {
        return outboxMessageRepository.save(outboxMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<OutboxMessage> findById(UUID id) {
        return outboxMessageRepository.findById(id);
    }
}
