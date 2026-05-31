package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.contracts.enums.service.client.AggregateType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий outbox-сообщений.
 */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    /**
     * Блокирует следующую пачку outbox-записей со статусом NEW.
     *
     * @param limit максимальное количество записей
     * @return заблокированные outbox-записи
     */
    @Query(value = """
            SELECT *
            FROM outbox_message
            WHERE status = 'NEW'
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxMessage> lockNextNewBatch(@Param("limit") int limit);

    /**
     * Проверяет наличие outbox-записей агрегата в указанных статусах.
     *
     * @param aggregateId   идентификатор агрегата
     * @param aggregateType тип агрегата
     * @param statuses      статусы
     * @return true, если есть хотя бы одна запись
     */
    boolean existsByAggregateIdAndAggregateTypeAndStatusIn(UUID aggregateId,
                                                           AggregateType aggregateType,
                                                           Iterable<OutboxStatus> statuses);

    /**
     * Подсчитывает outbox-записи агрегата в указанном статусе.
     *
     * @param aggregateId   идентификатор агрегата
     * @param aggregateType тип агрегата
     * @param status        статус
     * @return количество записей
     */
    long countByAggregateIdAndAggregateTypeAndStatus(UUID aggregateId,
                                                     AggregateType aggregateType,
                                                     OutboxStatus status);
}
