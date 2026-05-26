package ru.kubsu.clientservice.support;

import ru.kubsu.contracts.dto.service.client.ClientCreateRequest;
import ru.kubsu.contracts.dto.service.client.ClientUpdateRequest;
import ru.kubsu.contracts.enums.service.client.Gender;

import java.time.LocalDate;

/**
 * Фабрика тестовых DTO для REST-тестов клиентов.
 */
public final class ClientTestFixtures {

    private ClientTestFixtures() {
    }

    /**
     * Создаёт валидный запрос на создание клиента.
     *
     * @return запрос создания
     */
    public static ClientCreateRequest validCreateRequest() {
        return new ClientCreateRequest()
                .setLastName("Иванов")
                .setFirstName("Иван")
                .setPatronymic("Иванович")
                .setBirthDate(LocalDate.of(1990, 1, 15))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries("1234")
                .setIdentityDocumentNumber("567890")
                .setIdentityDocumentIssueDate(LocalDate.of(2010, 5, 20))
                .setItn("123456789012")
                .setInsuranceNumber("123-456-789 00")
                .setAddressDefined(true)
                .setResidenceAddressName("г. Москва, ул. Примерная, д. 1");
    }

    /**
     * Создаёт валидный запрос на обновление клиента.
     *
     * @return запрос обновления
     */
    public static ClientUpdateRequest validUpdateRequest() {
        return new ClientUpdateRequest()
                .setLastName("Петров")
                .setFirstName("Пётр")
                .setPatronymic("Петрович")
                .setBirthDate(LocalDate.of(1985, 3, 10))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries("4321")
                .setIdentityDocumentNumber("098765")
                .setIdentityDocumentIssueDate(LocalDate.of(2015, 6, 1))
                .setItn("210987654321")
                .setInsuranceNumber("987-654-321 00")
                .setAddressDefined(true)
                .setResidenceAddressName("г. Санкт-Петербург, пр. Невский, д. 10");
    }

    /**
     * Создаёт запрос на создание клиента с указанной фамилией и адресом.
     *
     * @param lastName              фамилия
     * @param residenceAddressName  адрес
     * @return запрос создания
     */
    public static ClientCreateRequest createRequestWithLastNameAndAddress(String lastName, String residenceAddressName) {
        return validCreateRequest()
                .setLastName(lastName)
                .setResidenceAddressName(residenceAddressName);
    }
}
