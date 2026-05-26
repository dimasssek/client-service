package ru.kubsu.clientservice.service;

import ru.kubsu.clientservice.entity.Client;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис персистентности клиентов.
 */
public interface ClientPersistenceService {

    /**
     * Сохраняет клиента в базе данных.
     *
     * @param client сущность клиента
     * @return сохранённый клиент
     */
    Client save(Client client);

    /**
     * Находит клиента по идентификатору.
     *
     * @param id идентификатор клиента
     * @return клиент, если найден
     */
    Optional<Client> findById(UUID id);
}
