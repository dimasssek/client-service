package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.repository.ClientRepository;
import ru.kubsu.clientservice.repository.OutboxMessageRepository;
import ru.kubsu.clientservice.repository.ResponseBatchRepository;
import ru.kubsu.clientservice.response.BroadcastProcessor;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.ExternalResponseTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;
import ru.kubsu.contracts.messaging.service.client.ExternalBroadcastMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты обработки рассылок от ведомств.
 */
class BroadcastProcessorIT extends IntegrationTestBase {

    /** Обработчик рассылки. */
    @Autowired
    private BroadcastProcessor broadcastProcessor;

    /** Репозиторий клиентов. */
    @Autowired
    private ClientRepository clientRepository;

    /** Репозиторий пачек ответов. */
    @Autowired
    private ResponseBatchRepository responseBatchRepository;

    /** Репозиторий outbox-сообщений. */
    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /**
     * Проверяет обновление клиента при совпадении по паспорту.
     */
    @Test
    void should_updateClient_whenBroadcastMatchesByPassport() {
        Client savedClient = clientPersistenceService.save(EntityTestFixtures.validClient());
        ExternalBroadcastMessage broadcast = ExternalResponseTestFixtures.updatedBroadcast(
                savedClient.getIdentityDocumentSeries(),
                savedClient.getIdentityDocumentNumber());

        broadcastProcessor.process(broadcast);

        Client updatedClient = clientRepository.findById(savedClient.getId()).orElseThrow();
        assertThat(updatedClient.getLastName()).isEqualTo("Сидоров");
        assertThat(responseBatchRepository.findAll()).hasSize(1);
        assertThat(outboxMessageRepository.findAll().stream()
                .anyMatch(message -> message.getEventType() == OutboxEventType.CLIENT_UPDATED
                        && message.getStatus() == OutboxStatus.NEW)).isTrue();
    }

    /**
     * Проверяет, что рассылка без совпадения не создаёт пачку ответа.
     */
    @Test
    void should_ignoreBroadcast_whenClientNotFound() {
        ExternalBroadcastMessage broadcast = ExternalResponseTestFixtures.updatedBroadcast("9999", "000000");

        broadcastProcessor.process(broadcast);

        assertThat(responseBatchRepository.findAll()).isEmpty();
    }
}
