package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.ExternalResponse;
import ru.kubsu.clientservice.repository.ExternalResponseRepository;
import ru.kubsu.clientservice.service.ExternalResponsePersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности внешних ответов.
 */
@Service
@RequiredArgsConstructor
public class ExternalResponsePersistenceServiceImpl implements ExternalResponsePersistenceService {

    /** Репозиторий внешних ответов. */
    private final ExternalResponseRepository externalResponseRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExternalResponse save(ExternalResponse externalResponse) {
        return externalResponseRepository.save(externalResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalResponse> findById(UUID id) {
        return externalResponseRepository.findById(id);
    }
}
