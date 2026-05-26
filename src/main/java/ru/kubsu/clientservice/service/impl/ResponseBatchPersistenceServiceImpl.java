package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.ResponseBatch;
import ru.kubsu.clientservice.repository.ResponseBatchRepository;
import ru.kubsu.clientservice.service.ResponseBatchPersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности пачек ответов.
 */
@Service
@RequiredArgsConstructor
public class ResponseBatchPersistenceServiceImpl implements ResponseBatchPersistenceService {

    /** Репозиторий пачек ответов. */
    private final ResponseBatchRepository responseBatchRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResponseBatch save(ResponseBatch responseBatch) {
        return responseBatchRepository.save(responseBatch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ResponseBatch> findById(UUID id) {
        return responseBatchRepository.findById(id);
    }
}
