package ru.kubsu.clientservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.entity.Request;
import ru.kubsu.clientservice.outbox.OutboxPublishProcessor;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.clientservice.service.ClientPersistenceService;
import ru.kubsu.clientservice.support.EntityTestFixtures;
import ru.kubsu.clientservice.support.ExternalRequestTestFixtures;
import ru.kubsu.clientservice.support.IntegrationTestBase;
import ru.kubsu.contracts.dto.service.client.ExternalRequestBatchCreateRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestTo;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E-тест эмулятора ведомства через RabbitMQ.
 *
 * <p>Запускается последним (префикс Z), данные не откатываются — после теста выполняется очистка БД.
 */
@TestPropertySource(properties = {
        "app.emulator.enabled=true",
        "app.emulator.min-delay-ms=200",
        "app.emulator.max-delay-ms=500"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ZExternalDepartmentEmulatorIT extends IntegrationTestBase {

    /** Обработчик публикации outbox. */
    @Autowired
    private OutboxPublishProcessor outboxPublishProcessor;

    /** Репозиторий запросов. */
    @Autowired
    private RequestRepository requestRepository;

    /** Сервис персистентности клиентов. */
    @Autowired
    private ClientPersistenceService clientPersistenceService;

    /** JDBC-шаблон для очистки данных после E2E-теста. */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Очищает данные, созданные E2E-тестом без отката транзакции.
     */
    @AfterEach
    void cleanupDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE response, external_response, response_batch, outbox_message,
                request, batch_request, external_request, client CASCADE
                """);
    }

    /**
     * Проверяет полный цикл: outbox → эмулятор → listener → outcome запроса.
     */
    @Test
    void should_processResponseEndToEnd_whenEmulatorEnabled() throws Exception {
        UUID clientId = clientPersistenceService.save(EntityTestFixtures.validClient()).getId();
        ExternalRequestTo externalRequest = createExternalRequest(List.of(clientId));
        UUID batchRequestId = externalRequest.getBatches().getFirst().getId();

        outboxPublishProcessor.publishPendingMessages();

        RequestOutcome outcome = waitForOutcome(batchRequestId, 15_000);
        assertThat(outcome).isIn(RequestOutcome.UPDATED, RequestOutcome.ACTUAL, RequestOutcome.NOT_FOUND);
    }

    /**
     * Ожидает завершения обработки ответа эмулятором.
     *
     * @param batchRequestId идентификатор пакетного запроса
     * @param timeoutMs      таймаут ожидания
     * @return итог обработки
     */
    private RequestOutcome waitForOutcome(UUID batchRequestId, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            List<Request> requests = requestRepository.findByBatchRequest_IdOrderByIdAsc(batchRequestId);
            if (!requests.isEmpty()) {
                RequestOutcome outcome = requests.getFirst().getOutcome();
                if (outcome != null && outcome != RequestOutcome.PENDING) {
                    return outcome;
                }
            }
            Thread.sleep(200);
        }
        throw new AssertionError("Outcome не получен за " + timeoutMs + " ms");
    }

    /**
     * Создаёт внешний запрос через API.
     *
     * @param clientIds идентификаторы клиентов
     * @return агрегированный внешний запрос
     */
    private ExternalRequestTo createExternalRequest(List<UUID> clientIds) throws Exception {
        ExternalRequestBatchCreateRequest request = ExternalRequestTestFixtures.validBatchCreateRequest(clientIds);
        String responseJson = mockMvc.perform(post("/external-requests/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return jsonMapper.readValue(responseJson, ExternalRequestTo.class);
    }
}
