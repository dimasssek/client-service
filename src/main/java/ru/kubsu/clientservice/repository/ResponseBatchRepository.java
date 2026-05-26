package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.ResponseBatch;

import java.util.UUID;

/**
 * Репозиторий пачек ответов.
 */
public interface ResponseBatchRepository extends JpaRepository<ResponseBatch, UUID> {
}
