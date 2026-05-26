package ru.kubsu.clientservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Пакетный запрос, входящий во внешний запрос.
 */
@Entity
@Table(name = "batch_request")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class BatchRequest {

    /** Идентификатор пакетного запроса. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Идентификатор сообщения. */
    @Column(name = "message_id")
    private UUID messageId;

    /** Дата создания/получения пакета. */
    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    /** Внешний запрос, частью которого является пакет. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ExternalRequest externalRequest;

    /** Количество записей в пакете. */
    @Column(name = "message_count")
    private Integer messageCount;
}
