package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.Request;

import java.util.UUID;

/**
 * Репозиторий запросов по клиентам.
 */
public interface RequestRepository extends JpaRepository<Request, UUID> {
}
