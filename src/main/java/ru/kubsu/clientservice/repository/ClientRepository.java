package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.kubsu.clientservice.entity.Client;

import java.util.UUID;

/**
 * Репозиторий клиентов с поддержкой QueryDSL-предикатов.
 */
public interface ClientRepository extends JpaRepository<Client, UUID>, QuerydslPredicateExecutor<Client> {
}
