package ru.kubsu.clientservice.response;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.entity.Response;
import ru.kubsu.clientservice.outbox.OutboxWriter;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.messaging.service.client.ExternalBroadcastMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Обновление клиента данными из ответа или рассылки ведомства.
 */
@Component
@RequiredArgsConstructor
public class ClientResponseUpdater {

    /** Репозиторий клиентов. */
    private final ClientRepository clientRepository;

    /** Запись outbox-сообщений. */
    private final OutboxWriter outboxWriter;

    /**
     * Обновляет клиента данными из элемента ответа и записывает outbox-событие.
     *
     * @param client клиент
     * @param item   элемент ответа
     * @return обновлённый клиент
     */
    public Client updateFromResponseItem(Client client, ExternalResponseItemMessage item) {
        applyItemData(client, item);
        Client savedClient = clientRepository.save(client);
        outboxWriter.writeClientEvent(savedClient, OutboxEventType.CLIENT_UPDATED);
        return savedClient;
    }

    /**
     * Обновляет клиента данными из рассылки и записывает outbox-событие.
     *
     * @param client    клиент
     * @param broadcast сообщение рассылки
     * @return обновлённый клиент
     */
    public Client updateFromBroadcast(Client client, ExternalBroadcastMessage broadcast) {
        applyBroadcastData(client, broadcast);
        Client savedClient = clientRepository.save(client);
        outboxWriter.writeClientEvent(savedClient, OutboxEventType.CLIENT_UPDATED);
        return savedClient;
    }

    /**
     * Проверяет, совпадают ли данные ручного запроса с ответом ведомства.
     *
     * @param request запрос
     * @param item    элемент ответа
     * @return true, если данные совпадают
     */
    public boolean manualRequestMatchesResponse(Request request, ExternalResponseItemMessage item) {
        return Objects.equals(request.getFirstName(), item.getFirstName())
                && Objects.equals(request.getLastName(), item.getLastName())
                && Objects.equals(request.getPatronymic(), item.getPatronymic())
                && Objects.equals(request.getBirthDate(), item.getBirthDate())
                && request.getGender() == item.getGender()
                && Objects.equals(request.getIdentityDocumentSeries(), item.getIdentityDocumentSeries())
                && Objects.equals(request.getIdentityDocumentNumber(), item.getIdentityDocumentNumber())
                && Objects.equals(request.getIdentityDocumentIssueDate(), item.getIdentityDocumentIssueDate())
                && Objects.equals(request.getItn(), item.getItn())
                && Objects.equals(request.getInsuranceNumber(), item.getInsuranceNumber());
    }

    /**
     * Создаёт сущность Response из элемента ответа.
     *
     * @param item элемент ответа
     * @return данные клиента из ответа
     */
    public Response toResponseEntity(ExternalResponseItemMessage item) {
        return new Response()
                .setFirstName(item.getFirstName())
                .setLastName(item.getLastName())
                .setPatronymic(item.getPatronymic())
                .setBirthDate(item.getBirthDate())
                .setGender(item.getGender())
                .setIdentityDocumentSeries(item.getIdentityDocumentSeries())
                .setIdentityDocumentNumber(item.getIdentityDocumentNumber())
                .setIdentityDocumentIssueDate(item.getIdentityDocumentIssueDate())
                .setItn(item.getItn())
                .setInsuranceNumber(item.getInsuranceNumber())
                .setResidenceAddressName(item.getResidenceAddressName())
                .setActualDate(item.getActualDate() != null ? item.getActualDate() : OffsetDateTime.now());
    }

    /**
     * Применяет данные элемента ответа к клиенту.
     *
     * @param client клиент
     * @param item   элемент ответа
     */
    private void applyItemData(Client client, ExternalResponseItemMessage item) {
        client.setFirstName(item.getFirstName())
                .setLastName(item.getLastName())
                .setPatronymic(item.getPatronymic())
                .setBirthDate(item.getBirthDate())
                .setGender(item.getGender())
                .setIdentityDocumentSeries(item.getIdentityDocumentSeries())
                .setIdentityDocumentNumber(item.getIdentityDocumentNumber())
                .setIdentityDocumentIssueDate(item.getIdentityDocumentIssueDate())
                .setItn(item.getItn())
                .setInsuranceNumber(item.getInsuranceNumber())
                .setResidenceAddressName(item.getResidenceAddressName())
                .setAddressDefined(true)
                .setActualDate(item.getActualDate() != null ? item.getActualDate() : OffsetDateTime.now());
    }

    /**
     * Применяет данные рассылки к клиенту.
     *
     * @param client    клиент
     * @param broadcast сообщение рассылки
     */
    private void applyBroadcastData(Client client, ExternalBroadcastMessage broadcast) {
        client.setFirstName(broadcast.getFirstName())
                .setLastName(broadcast.getLastName())
                .setPatronymic(broadcast.getPatronymic())
                .setBirthDate(broadcast.getBirthDate())
                .setGender(broadcast.getGender())
                .setIdentityDocumentSeries(broadcast.getIdentityDocumentSeries())
                .setIdentityDocumentNumber(broadcast.getIdentityDocumentNumber())
                .setIdentityDocumentIssueDate(broadcast.getIdentityDocumentIssueDate())
                .setItn(broadcast.getItn())
                .setInsuranceNumber(broadcast.getInsuranceNumber())
                .setResidenceAddressName(broadcast.getResidenceAddressName())
                .setAddressDefined(true)
                .setActualDate(broadcast.getActualDate() != null ? broadcast.getActualDate() : OffsetDateTime.now());
    }
}
