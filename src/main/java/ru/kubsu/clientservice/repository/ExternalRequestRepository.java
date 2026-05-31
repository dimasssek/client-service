package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.kubsu.clientservice.entity.ExternalRequest;

import java.util.UUID;

/**
 * Репозиторий внешних запросов с поддержкой QueryDSL-предикатов.
 */
public interface ExternalRequestRepository extends JpaRepository<ExternalRequest, UUID>,
        QuerydslPredicateExecutor<ExternalRequest> {
}
