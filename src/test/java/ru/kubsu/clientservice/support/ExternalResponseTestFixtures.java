package ru.kubsu.clientservice.support;

import ru.kubsu.contracts.enums.service.client.Gender;
import ru.kubsu.contracts.enums.service.client.SourceType;
import ru.kubsu.contracts.messaging.service.client.ExternalBroadcastMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Фабрика тестовых сообщений ответов ведомств.
 */
public final class ExternalResponseTestFixtures {

    private ExternalResponseTestFixtures() {
    }

    /**
     * Формирует полный ответ по пачке с данными, совпадающими с клиентом.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @param correlationId  correlationId запроса
     * @return сообщение ответа
     */
    public static ExternalResponseBatchMessage matchingResponse(UUID batchRequestId, UUID correlationId) {
        ExternalResponseItemMessage item = new ExternalResponseItemMessage()
                .setCorrelationId(correlationId)
                .setFirstName("Иван")
                .setLastName("Иванов")
                .setPatronymic("Иванович")
                .setBirthDate(LocalDate.of(1990, 1, 15))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries("1234")
                .setIdentityDocumentNumber("567890")
                .setIdentityDocumentIssueDate(LocalDate.of(2010, 5, 20))
                .setItn("123456789012")
                .setInsuranceNumber("123-456-789 00")
                .setResidenceAddressName("г. Москва, ул. Примерная, д. 1")
                .setActualDate(OffsetDateTime.now());

        return new ExternalResponseBatchMessage()
                .setBatchRequestId(batchRequestId)
                .setBatchMessageId(UUID.randomUUID())
                .setSourceType(SourceType.FNS)
                .setItems(List.of(item));
    }

    /**
     * Формирует ответ с обновлённой фамилией клиента.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @param correlationId  correlationId запроса
     * @return сообщение ответа
     */
    public static ExternalResponseBatchMessage updatedResponse(UUID batchRequestId, UUID correlationId) {
        ExternalResponseBatchMessage message = matchingResponse(batchRequestId, correlationId);
        message.getItems().getFirst().setLastName("Петров");
        return message;
    }

    /**
     * Формирует пустой ответ (все запросы не найдены).
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @return сообщение ответа
     */
    public static ExternalResponseBatchMessage emptyResponse(UUID batchRequestId) {
        return new ExternalResponseBatchMessage()
                .setBatchRequestId(batchRequestId)
                .setBatchMessageId(UUID.randomUUID())
                .setSourceType(SourceType.FNS)
                .setItems(List.of());
    }

    /**
     * Формирует рассылку с обновлённой фамилией.
     *
     * @param clientSeries серия документа клиента
     * @param clientNumber номер документа клиента
     * @return сообщение рассылки
     */
    public static ExternalBroadcastMessage updatedBroadcast(String clientSeries, String clientNumber) {
        return new ExternalBroadcastMessage()
                .setSourceType(SourceType.FNS)
                .setMessageId(UUID.randomUUID())
                .setFirstName("Иван")
                .setLastName("Сидоров")
                .setPatronymic("Иванович")
                .setBirthDate(LocalDate.of(1990, 1, 15))
                .setGender(Gender.MALE)
                .setIdentityDocumentSeries(clientSeries)
                .setIdentityDocumentNumber(clientNumber)
                .setIdentityDocumentIssueDate(LocalDate.of(2010, 5, 20))
                .setItn("123456789012")
                .setInsuranceNumber("123-456-789 00")
                .setResidenceAddressName("г. Москва, ул. Рассылочная, д. 2")
                .setActualDate(OffsetDateTime.now());
    }
}
