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
import ru.kubsu.contracts.enums.service.client.Gender;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.enums.service.client.RequestType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Запрос по конкретному клиенту в рамках пакетного запроса.
 */
@Entity
@Table(name = "request")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Request {

    /** Идентификатор запроса. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Клиент, по которому выполняется запрос. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    /** Идентификатор сообщения (correlationId). */
    @Column(name = "message_id")
    private UUID messageId;

    /** Пакетный запрос, частью которого является запрос. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_request_id")
    private BatchRequest batchRequest;

    /** Статус запроса. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

    /** Детализация ошибки. */
    @Column(name = "error_message")
    private String errorMessage;

    /** Итог обработки ответа ведомства. */
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private RequestOutcome outcome;

    /** Имя. */
    @Column(name = "first_name")
    private String firstName;

    /** Фамилия. */
    @Column(name = "last_name")
    private String lastName;

    /** Отчество. */
    @Column(name = "patronymic")
    private String patronymic;

    /** Дата рождения. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Пол. */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    /** Серия документа. */
    @Column(name = "identity_document_series", length = 20)
    private String identityDocumentSeries;

    /** Номер документа. */
    @Column(name = "identity_document_number", length = 50)
    private String identityDocumentNumber;

    /** Дата выдачи документа. */
    @Column(name = "identity_document_issue_date")
    private LocalDate identityDocumentIssueDate;

    /** ИНН. */
    @Column(name = "itn", length = 12)
    private String itn;

    /** СНИЛС. */
    @Column(name = "insurance_number", length = 20)
    private String insuranceNumber;

    /** Тип запроса. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private RequestType type;
}
