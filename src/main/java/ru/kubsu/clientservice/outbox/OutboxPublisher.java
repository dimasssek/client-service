package ru.kubsu.clientservice.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик публикации outbox-сообщений в RabbitMQ.
 *
 * <p>Приём ответов от ведомств, сопоставление correlationId, обновление {@code Client}
 * и summary по запросу — этап 5 ({@code ExternalResponseListener}).
 */
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    /** Обработчик публикации outbox-записей. */
    private final OutboxPublishProcessor outboxPublishProcessor;

    /**
     * Периодически публикует outbox-записи со статусом NEW.
     */
    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms}")
    public void pollOutbox() {
        outboxPublishProcessor.publishPendingMessages();
    }
}
