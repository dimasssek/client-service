package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.BatchRequest;
import ru.kubsu.clientservice.repository.BatchRequestRepository;
import ru.kubsu.clientservice.service.BatchRequestPersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности пакетных запросов.
 */
@Service
@RequiredArgsConstructor
public class BatchRequestPersistenceServiceImpl implements BatchRequestPersistenceService {

    /** Репозиторий пакетных запросов. */
    private final BatchRequestRepository batchRequestRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BatchRequest save(BatchRequest batchRequest) {
        return batchRequestRepository.save(batchRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BatchRequest> findById(UUID id) {
        return batchRequestRepository.findById(id);
    }
}
