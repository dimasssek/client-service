package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kubsu.clientservice.entity.BatchRequest;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий пакетных запросов.
 */
public interface BatchRequestRepository extends JpaRepository<BatchRequest, UUID> {

    /**
     * Возвращает пакетные запросы по идентификатору внешнего запроса.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @return список пакетных запросов
     */
    List<BatchRequest> findByExternalRequest_IdOrderByCreatedDateAsc(UUID externalRequestId);

    /**
     * Подсчитывает количество пачек по идентификаторам внешних запросов.
     *
     * @param externalRequestIds идентификаторы внешних запросов
     * @return пары [externalRequestId, count]
     */
    @Query("""
            SELECT br.externalRequest.id, COUNT(br)
            FROM BatchRequest br
            WHERE br.externalRequest.id IN :externalRequestIds
            GROUP BY br.externalRequest.id
            """)
    List<Object[]> countByExternalRequestIds(@Param("externalRequestIds") Collection<UUID> externalRequestIds);
}
