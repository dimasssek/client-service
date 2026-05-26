package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.Client;

import java.util.UUID;

/**
 * Репозиторий клиентов.
 */
public interface ClientRepository extends JpaRepository<Client, UUID> {
}
