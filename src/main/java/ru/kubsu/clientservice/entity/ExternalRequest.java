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
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.enums.service.client.SourceType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Внешний запрос к государственному источнику.
 */
@Entity
@Table(name = "external_request")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ExternalRequest {

    /** Уникальный идентификатор внешнего запроса. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Номер исходящего письма. */
    @Column(name = "letter_number", nullable = false)
    private String letterNumber;

    /** Дата исходящего запроса. */
    @Column(name = "letter_date", nullable = false)
    private LocalDate letterDate;

    /** Тип внешнего источника-получателя. */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    /** Статус запроса. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    /** Логин пользователя, инициировавшего запрос. */
    @Column(name = "initiator_login", nullable = false)
    private String initiatorLogin;

    /** Дата создания запроса. */
    @Column(name = "created", nullable = false)
    private OffsetDateTime created;
}
