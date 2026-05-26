package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.RequestPersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности запросов по клиентам.
 */
@Service
@RequiredArgsConstructor
public class RequestPersistenceServiceImpl implements RequestPersistenceService {

    /** Репозиторий запросов. */
    private final RequestRepository requestRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Request> findById(UUID id) {
        return requestRepository.findById(id);
    }
}
