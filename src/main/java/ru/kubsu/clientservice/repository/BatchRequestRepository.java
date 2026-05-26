package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.BatchRequest;

import java.util.UUID;

/**
 * Репозиторий пакетных запросов.
 */
public interface BatchRequestRepository extends JpaRepository<BatchRequest, UUID> {
}
