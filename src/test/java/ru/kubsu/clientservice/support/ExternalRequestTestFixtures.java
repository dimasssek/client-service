package ru.kubsu.clientservice.support;

import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestManualCreateRequest;
import ru.kubsu.contracts.enums.service.client.Gender;
import ru.kubsu.contracts.enums.service.client.SourceType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Фабрика тестовых DTO для REST-тестов внешних запросов.
 */
public final class ExternalRequestTestFixtures {

    private ExternalRequestTestFixtures() {
    }

    /**
     * Создаёт валидный запрос на batch-создание внешнего запроса.
     *
     * @param clientIds идентификаторы клиентов
     * @return запрос создания
     */
    public static ExternalRequestBatchCreateRequest validBatchCreateRequest(List<UUID> clientIds) {
        return new ExternalRequestBatchCreateRequest()
                .setLetterNumber("001")
                .setLetterDate(LocalDate.of(2026, 5, 31))
                .setSourceType(SourceType.FNS)
                .setInitiatorLogin("operator")
                .setClientIds(clientIds);
    }

    /**
     * Создаёт валидный запрос на manual-создание внешнего запроса.
     *
     * @return запрос создания
     */
    public static ExternalRequestManualCreateRequest validManualCreateRequest() {
        return new ExternalRequestManualCreateRequest()
                .setLetterNumber("002")
                .setLetterDate(LocalDate.of(2026, 5, 31))
                .setSourceType(SourceType.EPGU)
                .setInitiatorLogin("operator")
                .setLastName("Петров")
                .setFirstName("Пётр")
                .setPatronymic("Петрович")
                .setBirthDate(LocalDate.of(1985, 3, 10))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries("4321")
                .setIdentityDocumentNumber("098765")
                .setIdentityDocumentIssueDate(LocalDate.of(2015, 6, 1))
                .setItn("210987654321")
                .setInsuranceNumber("987-654-321 00");
    }
}
