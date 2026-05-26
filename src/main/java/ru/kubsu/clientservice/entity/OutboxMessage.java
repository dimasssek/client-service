package ru.kubsu.clientservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.kubsu.contracts.enums.service.client.AggregateType;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Outbox-сообщение для надёжной публикации в RabbitMQ.
 */
@Entity
@Table(name = "outbox_message")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OutboxMessage {

    /** Идентификатор outbox-записи. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Тип агрегата. */
    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false)
    private AggregateType aggregateType;

    /** Идентификатор агрегата. */
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    /** Тип события. */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private OutboxEventType eventType;

    /** Тело сообщения в формате JSON. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private String payload;

    /** Имя exchange для публикации. */
    @Column(name = "exchange_name", nullable = false)
    private String exchangeName;

    /** Routing key для публикации. */
    @Column(name = "routing_key", nullable = false)
    private String routingKey;

    /** Статус обработки outbox-записи. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    /** Количество попыток отправки. */
    @Column(name = "attempts", nullable = false)
    private int attempts;

    /** Текст последней ошибки отправки. */
    @Column(name = "last_error")
    private String lastError;

    /** Дата создания outbox-записи. */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Дата успешной отправки. */
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;
}
