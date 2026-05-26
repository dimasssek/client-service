package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.repository.ExternalRequestRepository;
import ru.kubsu.clientservice.service.ExternalRequestPersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности внешних запросов.
 */
@Service
@RequiredArgsConstructor
public class ExternalRequestPersistenceServiceImpl implements ExternalRequestPersistenceService {

    /** Репозиторий внешних запросов. */
    private final ExternalRequestRepository externalRequestRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExternalRequest save(ExternalRequest externalRequest) {
        return externalRequestRepository.save(externalRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalRequest> findById(UUID id) {
        return externalRequestRepository.findById(id);
    }
}
