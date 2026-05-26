package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.Response;
import ru.kubsu.clientservice.repository.ResponseRepository;
import ru.kubsu.clientservice.service.ResponsePersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности данных клиента из ответа.
 */
@Service
@RequiredArgsConstructor
public class ResponsePersistenceServiceImpl implements ResponsePersistenceService {

    /** Репозиторий данных клиента из ответа. */
    private final ResponseRepository responseRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Response save(Response response) {
        return responseRepository.save(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Response> findById(UUID id) {
        return responseRepository.findById(id);
    }
}
