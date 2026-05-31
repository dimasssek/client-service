package ru.kubsu.clientservice.response;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.contracts.messaging.service.client.ExternalBroadcastMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;

import java.util.Objects;

/**
 * Сравнение данных клиента с ответом ведомства.
 */
@Component
@RequiredArgsConstructor
public class ClientResponseComparator {

    /**
     * Проверяет, отличаются ли данные клиента от ответа ведомства.
     *
     * @param client клиент
     * @param item   элемент ответа
     * @return true, если требуется обновление клиента
     */
    public boolean needsUpdate(Client client, ExternalResponseItemMessage item) {
        return !Objects.equals(client.getFirstName(), item.getFirstName())
                || !Objects.equals(client.getLastName(), item.getLastName())
                || !Objects.equals(client.getPatronymic(), item.getPatronymic())
                || !Objects.equals(client.getBirthDate(), item.getBirthDate())
                || client.getGender() != item.getGender()
                || !Objects.equals(client.getIdentityDocumentSeries(), item.getIdentityDocumentSeries())
                || !Objects.equals(client.getIdentityDocumentNumber(), item.getIdentityDocumentNumber())
                || !Objects.equals(client.getIdentityDocumentIssueDate(), item.getIdentityDocumentIssueDate())
                || !Objects.equals(client.getItn(), item.getItn())
                || !Objects.equals(client.getInsuranceNumber(), item.getInsuranceNumber())
                || !Objects.equals(client.getResidenceAddressName(), item.getResidenceAddressName());
    }

    /**
     * Проверяет, отличаются ли данные клиента от рассылки ведомства.
     *
     * @param client    клиент
     * @param broadcast сообщение рассылки
     * @return true, если требуется обновление клиента
     */
    public boolean needsUpdate(Client client, ExternalBroadcastMessage broadcast) {
        return !Objects.equals(client.getFirstName(), broadcast.getFirstName())
                || !Objects.equals(client.getLastName(), broadcast.getLastName())
                || !Objects.equals(client.getPatronymic(), broadcast.getPatronymic())
                || !Objects.equals(client.getBirthDate(), broadcast.getBirthDate())
                || client.getGender() != broadcast.getGender()
                || !Objects.equals(client.getIdentityDocumentSeries(), broadcast.getIdentityDocumentSeries())
                || !Objects.equals(client.getIdentityDocumentNumber(), broadcast.getIdentityDocumentNumber())
                || !Objects.equals(client.getIdentityDocumentIssueDate(), broadcast.getIdentityDocumentIssueDate())
                || !Objects.equals(client.getItn(), broadcast.getItn())
                || !Objects.equals(client.getInsuranceNumber(), broadcast.getInsuranceNumber())
                || !Objects.equals(client.getResidenceAddressName(), broadcast.getResidenceAddressName());
    }
}
