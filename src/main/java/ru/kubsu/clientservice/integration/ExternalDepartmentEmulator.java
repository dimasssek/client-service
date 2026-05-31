package ru.kubsu.clientservice.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import ru.kubsu.clientservice.config.EmulatorProperties;
import ru.kubsu.clientservice.config.RabbitQueueProperties;
import ru.kubsu.contracts.enums.service.client.SourceType;
import ru.kubsu.contracts.messaging.service.client.ExternalRequestBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalRequestItemMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseBatchMessage;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseItemMessage;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Эмулятор внешних ведомств: читает исходящие запросы и публикует ответы с задержкой.
 */
@Component
@ConditionalOnProperty(prefix = "app.emulator", name = "enabled", havingValue = "true")
@Slf4j
public class ExternalDepartmentEmulator {

    private static final String[] UPDATED_LAST_NAMES = {"Петров", "Сидоров", "Козлов", "Новиков"};

    /** Свойства эмулятора. */
    private final EmulatorProperties emulatorProperties;

    /** Свойства имён RabbitMQ. */
    private final RabbitQueueProperties rabbitQueueProperties;

    /** Шаблон публикации в RabbitMQ. */
    private final RabbitTemplate rabbitTemplate;

    /** JSON-сериализатор. */
    private final JsonMapper jsonMapper;

    /** Исполнитель фоновых задач эмулятора. */
    private final TaskExecutor taskExecutor;

    /**
     * @param emulatorProperties  свойства эмулятора
     * @param rabbitQueueProperties свойства RabbitMQ
     * @param rabbitTemplate      шаблон публикации
     * @param jsonMapper          JSON-сериализатор
     * @param taskExecutor        application task executor
     */
    public ExternalDepartmentEmulator(EmulatorProperties emulatorProperties,
                                      RabbitQueueProperties rabbitQueueProperties,
                                      RabbitTemplate rabbitTemplate,
                                      JsonMapper jsonMapper,
                                      @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.emulatorProperties = emulatorProperties;
        this.rabbitQueueProperties = rabbitQueueProperties;
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Обрабатывает запрос в очередь ФНС.
     *
     * @param payload JSON-сообщение
     */
    @RabbitListener(queues = "${app.rabbit.queue.external-request-fns}")
    public void onFnsRequest(String payload) {
        taskExecutor.execute(() -> handleRequest(payload, SourceType.FNS));
    }

    /**
     * Обрабатывает запрос в очередь ЕПГУ.
     *
     * @param payload JSON-сообщение
     */
    @RabbitListener(queues = "${app.rabbit.queue.external-request-epgu}")
    public void onEpguRequest(String payload) {
        taskExecutor.execute(() -> handleRequest(payload, SourceType.EPGU));
    }

    /**
     * Формирует и публикует ответ ведомства после задержки.
     *
     * @param payload    JSON исходного запроса
     * @param sourceType тип источника
     */
    protected void handleRequest(String payload, SourceType sourceType) {
        try {
            Thread.sleep(randomDelayMs());
            ExternalRequestBatchMessage requestMessage = jsonMapper.readValue(payload, ExternalRequestBatchMessage.class);
            ExternalResponseBatchMessage responseMessage = buildResponse(requestMessage, sourceType);
            String routingKey = resolveResponseRoutingKey(sourceType);
            rabbitTemplate.convertAndSend(
                    rabbitQueueProperties.getExchange().getExternalResponse(),
                    routingKey,
                    jsonMapper.writeValueAsString(responseMessage));
            log.debug("Эмулятор опубликовал ответ batchRequestId={}", requestMessage.getBatchRequestId());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Эмулятор прерван при обработке запроса sourceType={}", sourceType);
        } catch (Exception exception) {
            log.error("Ошибка эмулятора at ExternalDepartmentEmulator.handleRequest: {}", exception.getMessage());
        }
    }

    /**
     * Формирует ответ на пачку запроса с частичным покрытием и случайными изменениями данных.
     *
     * @param requestMessage исходный запрос
     * @param sourceType     тип источника
     * @return сообщение ответа
     */
    private ExternalResponseBatchMessage buildResponse(ExternalRequestBatchMessage requestMessage,
                                                       SourceType sourceType) {
        List<ExternalRequestItemMessage> requestItems = requestMessage.getItems() != null
                ? requestMessage.getItems()
                : List.of();
        List<ExternalRequestItemMessage> shuffledItems = new ArrayList<>(requestItems);
        Collections.shuffle(shuffledItems, ThreadLocalRandom.current());

        int respondCount = shuffledItems.isEmpty()
                ? 0
                : Math.max(1, shuffledItems.size() - ThreadLocalRandom.current().nextInt(0, 2));

        List<ExternalResponseItemMessage> responseItems = new ArrayList<>();
        for (int index = 0; index < respondCount; index++) {
            responseItems.add(toResponseItem(shuffledItems.get(index)));
        }

        return new ExternalResponseBatchMessage()
                .setBatchRequestId(requestMessage.getBatchRequestId())
                .setBatchMessageId(UUID.randomUUID())
                .setSourceType(sourceType)
                .setItems(responseItems);
    }

    /**
     * Преобразует элемент запроса в элемент ответа с возможным изменением данных.
     *
     * @param requestItem элемент запроса
     * @return элемент ответа
     */
    private ExternalResponseItemMessage toResponseItem(ExternalRequestItemMessage requestItem) {
        ExternalResponseItemMessage responseItem = new ExternalResponseItemMessage()
                .setCorrelationId(requestItem.getCorrelationId())
                .setFirstName(requestItem.getFirstName())
                .setLastName(requestItem.getLastName())
                .setPatronymic(requestItem.getPatronymic())
                .setBirthDate(requestItem.getBirthDate())
                .setGender(requestItem.getGender())
                .setIdentityDocumentSeries(requestItem.getIdentityDocumentSeries())
                .setIdentityDocumentNumber(requestItem.getIdentityDocumentNumber())
                .setIdentityDocumentIssueDate(requestItem.getIdentityDocumentIssueDate())
                .setItn(requestItem.getItn())
                .setInsuranceNumber(requestItem.getInsuranceNumber())
                .setResidenceAddressName("г. Москва, ул. Обновлённая, д. 1")
                .setActualDate(OffsetDateTime.now());

        if (ThreadLocalRandom.current().nextBoolean()) {
            Random random = ThreadLocalRandom.current();
            responseItem.setLastName(UPDATED_LAST_NAMES[random.nextInt(UPDATED_LAST_NAMES.length)]);
        }
        return responseItem;
    }

    /**
     * Определяет routing key ответа по типу источника.
     *
     * @param sourceType тип источника
     * @return routing key
     */
    private String resolveResponseRoutingKey(SourceType sourceType) {
        return switch (sourceType) {
            case FNS -> rabbitQueueProperties.getRoutingKey().getExternalResponseFns();
            case EPGU -> rabbitQueueProperties.getRoutingKey().getExternalResponseEpgu();
        };
    }

    /**
     * Возвращает случайную задержку ответа в миллисекундах.
     *
     * @return задержка
     */
    private long randomDelayMs() {
        long minDelay = emulatorProperties.getMinDelayMs();
        long maxDelay = emulatorProperties.getMaxDelayMs();
        if (maxDelay <= minDelay) {
            return minDelay;
        }
        return ThreadLocalRandom.current().nextLong(minDelay, maxDelay + 1);
    }
}
