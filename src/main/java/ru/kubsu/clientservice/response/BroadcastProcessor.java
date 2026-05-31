package ru.kubsu.clientservice.response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.ResponseBatch;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.repository.ResponseBatchRepository;
import ru.kubsu.contracts.messaging.service.client.ExternalBroadcastMessage;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Обработка рассылки данных от ведомства без предварительного запроса.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastProcessor {

    /** Репозиторий клиентов. */
    private final ClientRepository clientRepository;

    /** Репозиторий пачек ответов. */
    private final ResponseBatchRepository responseBatchRepository;

    /** Сравнение данных клиента с ответом. */
    private final ClientResponseComparator clientResponseComparator;

    /** Обновление клиента по ответу. */
    private final ClientResponseUpdater clientResponseUpdater;

    /**
     * Обрабатывает рассылку от ведомства.
     *
     * @param message сообщение рассылки
     */
    @Transactional
    public void process(ExternalBroadcastMessage message) {
        Optional<Client> matchedClient = findClient(message);
        if (matchedClient.isEmpty()) {
            log.info("Рассылка messageId={} не сопоставлена ни с одним клиентом", message.getMessageId());
            return;
        }

        Client client = matchedClient.get();
        ResponseBatch responseBatch = new ResponseBatch()
                .setBatchRequest(null)
                .setSourceType(message.getSourceType())
                .setMessageId(message.getMessageId())
                .setReceivedDate(OffsetDateTime.now());
        responseBatchRepository.save(responseBatch);

        if (clientResponseComparator.needsUpdate(client, message)) {
            clientResponseUpdater.updateFromBroadcast(client, message);
        }
    }

    /**
     * Ищет клиента по идентификаторам из рассылки.
     *
     * @param message сообщение рассылки
     * @return найденный клиент
     */
    private Optional<Client> findClient(ExternalBroadcastMessage message) {
        if (message.getIdentityDocumentSeries() != null && message.getIdentityDocumentNumber() != null) {
            Optional<Client> byDocument = clientRepository
                    .findByIdentityDocumentSeriesAndIdentityDocumentNumber(
                            message.getIdentityDocumentSeries(),
                            message.getIdentityDocumentNumber());
            if (byDocument.isPresent()) {
                return byDocument;
            }
        }
        if (message.getItn() != null) {
            return clientRepository.findByItn(message.getItn());
        }
        if (message.getInsuranceNumber() != null) {
            return clientRepository.findByInsuranceNumber(message.getInsuranceNumber());
        }
        return Optional.empty();
    }
}
