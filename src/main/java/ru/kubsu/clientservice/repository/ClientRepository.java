package ru.kubsu.clientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.kubsu.clientservice.entity.Client;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий клиентов с поддержкой QueryDSL-предикатов.
 */
public interface ClientRepository extends JpaRepository<Client, UUID>, QuerydslPredicateExecutor<Client> {

    /**
     * Ищет клиента по серии и номеру документа.
     *
     * @param identityDocumentSeries серия документа
     * @param identityDocumentNumber номер документа
     * @return клиент
     */
    Optional<Client> findByIdentityDocumentSeriesAndIdentityDocumentNumber(String identityDocumentSeries,
                                                                           String identityDocumentNumber);

    /**
     * Ищет клиента по ИНН.
     *
     * @param itn ИНН
     * @return клиент
     */
    Optional<Client> findByItn(String itn);

    /**
     * Ищет клиента по СНИЛС.
     *
     * @param insuranceNumber СНИЛС
     * @return клиент
     */
    Optional<Client> findByInsuranceNumber(String insuranceNumber);
}
