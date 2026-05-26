package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kubsu.clientservice.entity.Response;

import java.util.UUID;

/**
 * Репозиторий данных клиента из ответа внешнего источника.
 */
public interface ResponseRepository extends JpaRepository<Response, UUID> {
}
