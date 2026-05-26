package ru.kubsu.clientservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.kubsu.contracts.enums.service.client.SourceType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Пачка ответа от внешнего источника.
 */
@Entity
@Table(name = "response_batch")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ResponseBatch {

    /** Идентификатор пачки ответа. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Пакетный запрос, на который получен ответ. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_request_id")
    private BatchRequest batchRequest;

    /** Тип внешнего источника. */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    /** Идентификатор сообщения ответа. */
    @Column(name = "message_id")
    private UUID messageId;

    /** Дата получения пачки ответа. */
    @Column(name = "received_date", nullable = false)
    private OffsetDateTime receivedDate;
}
