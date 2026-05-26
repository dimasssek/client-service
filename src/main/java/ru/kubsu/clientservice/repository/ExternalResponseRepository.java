package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.ExternalResponse;

import java.util.UUID;

/**
 * Репозиторий внешних ответов.
 */
public interface ExternalResponseRepository extends JpaRepository<ExternalResponse, UUID> {
}
