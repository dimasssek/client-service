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
import ru.kubsu.contracts.enums.service.client.RequestStatus;

import java.util.UUID;

/**
 * Ответ по конкретному клиентскому запросу от внешнего источника.
 */
@Entity
@Table(name = "external_response")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ExternalResponse {

    /** Идентификатор ответа. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Пачка ответа. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_batch_id", nullable = false)
    private ResponseBatch responseBatch;

    /** Исходный запрос. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    /** Идентификатор корреляции. */
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    /** Клиент в системе. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    /** Статус ответа. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    /** Детализация ошибки. */
    @Column(name = "error_message")
    private String errorMessage;
}
