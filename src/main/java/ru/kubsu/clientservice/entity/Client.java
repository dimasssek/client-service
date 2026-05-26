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
import lombok.experimental.FieldNameConstants;
import ru.kubsu.contracts.enums.service.client.ClientStatus;
import ru.kubsu.contracts.enums.service.client.Gender;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Клиент банка.
 */
@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@FieldNameConstants
public class Client {

    /** Уникальный идентификатор клиента. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Фамилия. */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Имя. */
    @Column(name = "first_name", nullable = false)
    private String firstName;

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

    /** Серия паспорта. */
    @Column(name = "identity_document_series", nullable = false)
    private String identityDocumentSeries;

    /** Номер паспорта. */
    @Column(name = "identity_document_number")
    private String identityDocumentNumber;

    /** Дата выдачи паспорта. */
    @Column(name = "identity_document_issue_date")
    private LocalDate identityDocumentIssueDate;

    /** ИНН. */
    @Column(name = "itn", length = 12)
    private String itn;

    /** СНИЛС. */
    @Column(name = "insurance_number", length = 20)
    private String insuranceNumber;

    /** Последняя дата обновления записи. */
    @Column(name = "actual_date")
    private OffsetDateTime actualDate;

    /** Признак определённости адреса. */
    @Column(name = "is_address_defined", nullable = false)
    private boolean addressDefined;

    /** Адрес места жительства. */
    @Column(name = "residence_address_name", nullable = false)
    private String residenceAddressName;

    /** Статус клиента. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClientStatus status;
}
