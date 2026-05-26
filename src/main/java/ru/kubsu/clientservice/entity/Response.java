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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Данные клиента, полученные от внешнего источника.
 */
@Entity
@Table(name = "response")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Response {

    /** Идентификатор данных клиента из ответа. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Внешний ответ, к которому относятся данные. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_response_id", nullable = false)
    private ExternalResponse externalResponse;

    /** Имя. */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** Фамилия. */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Отчество. */
    @Column(name = "patronymic")
    private String patronymic;

    /** Дата рождения. */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** Пол. */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    /** Серия документа. */
    @Column(name = "identity_document_series", nullable = false)
    private String identityDocumentSeries;

    /** Номер документа. */
    @Column(name = "identity_document_number", nullable = false)
    private String identityDocumentNumber;

    /** Дата выдачи документа. */
    @Column(name = "identity_document_issue_date", nullable = false)
    private LocalDate identityDocumentIssueDate;

    /** ИНН. */
    @Column(name = "itn")
    private String itn;

    /** СНИЛС. */
    @Column(name = "insurance_number")
    private String insuranceNumber;

    /** Адрес места жительства. */
    @Column(name = "residence_address_name", nullable = false)
    private String residenceAddressName;

    /** Дата актуальности данных. */
    @Column(name = "actual_date")
    private OffsetDateTime actualDate;
}
