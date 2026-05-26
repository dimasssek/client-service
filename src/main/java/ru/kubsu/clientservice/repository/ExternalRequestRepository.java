package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.ExternalRequest;

import java.util.UUID;

/**
 * Репозиторий внешних запросов.
 */
public interface ExternalRequestRepository extends JpaRepository<ExternalRequest, UUID> {
}
