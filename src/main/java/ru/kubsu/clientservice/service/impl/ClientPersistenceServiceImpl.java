package ru.kubsu.clientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.service.ClientPersistenceService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса персистентности клиентов.
 */
@Service
@RequiredArgsConstructor
public class ClientPersistenceServiceImpl implements ClientPersistenceService {

    /** Репозиторий клиентов. */
    private final ClientRepository clientRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(UUID id) {
        return clientRepository.findById(id);
    }
}
