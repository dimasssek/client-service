package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;
import ru.kubsu.contracts.enums.service.client.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий запросов по клиентам.
 */
public interface RequestRepository extends JpaRepository<Request, UUID> {

    /**
     * Возвращает запросы по идентификатору пакетного запроса.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @return список запросов
     */
    List<Request> findByBatchRequest_IdOrderByIdAsc(UUID batchRequestId);

    /**
     * Возвращает запрос по идентификатору корреляции.
     *
     * @param messageId идентификатор сообщения
     * @return запрос
     */
    Optional<Request> findByMessageId(UUID messageId);

    /**
     * Подсчитывает запросы внешнего запроса, не находящиеся в указанном статусе.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @param status            статус для исключения
     * @return количество запросов
     */
    long countByBatchRequest_ExternalRequest_IdAndStatusNot(UUID externalRequestId, RequestStatus status);

    /**
     * Возвращает запросы клиента с загруженными внешними запросами для истории.
     *
     * @param clientId идентификатор клиента
     * @return список запросов
     */
    @Query("""
            SELECT r
            FROM Request r
            JOIN FETCH r.batchRequest br
            JOIN FETCH br.externalRequest er
            WHERE r.client.id = :clientId
            ORDER BY er.created DESC, r.id ASC
            """)
    List<Request> findHistoryByClientId(@Param("clientId") UUID clientId);

    /**
     * Подсчитывает количество запросов по идентификаторам внешних запросов.
     *
     * @param externalRequestIds идентификаторы внешних запросов
     * @return пары [externalRequestId, count]
     */
    @Query("""
            SELECT br.externalRequest.id, COUNT(r)
            FROM Request r
            JOIN r.batchRequest br
            WHERE br.externalRequest.id IN :externalRequestIds
            GROUP BY br.externalRequest.id
            """)
    List<Object[]> countByExternalRequestIds(@Param("externalRequestIds") Collection<UUID> externalRequestIds);

    /**
     * Обновляет статус запросов пакетного запроса.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @param status         новый статус
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Request r
            SET r.status = :status, r.outcome = :outcome
            WHERE r.batchRequest.id = :batchRequestId
            """)
    void updateStatusAndOutcomeByBatchRequestId(@Param("batchRequestId") UUID batchRequestId,
                                                @Param("status") RequestStatus status,
                                                @Param("outcome") RequestOutcome outcome);

    /**
     * Обновляет статус и текст ошибки запросов внешнего запроса.
     *
     * @param externalRequestId идентификатор внешнего запроса
     * @param status            новый статус
     * @param errorMessage      детализация ошибки
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Request r
            SET r.status = :status, r.errorMessage = :errorMessage, r.outcome = :outcome
            WHERE r.batchRequest.externalRequest.id = :externalRequestId
            """)
    void updateStatusErrorAndOutcomeByExternalRequestId(@Param("externalRequestId") UUID externalRequestId,
                                                        @Param("status") RequestStatus status,
                                                        @Param("errorMessage") String errorMessage,
                                                        @Param("outcome") RequestOutcome outcome);
}
