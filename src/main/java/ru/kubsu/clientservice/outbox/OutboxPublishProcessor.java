package ru.kubsu.clientservice.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kubsu.clientservice.config.OutboxProperties;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.OutboxMessage;
import ru.kubsu.clientservice.repository.ExternalRequestRepository;
import ru.kubsu.clientservice.repository.OutboxMessageRepository;
import ru.kubsu.clientservice.repository.RequestRepository;
import ru.kubsu.contracts.enums.service.client.AggregateType;
import ru.kubsu.contracts.enums.service.client.OutboxEventType;
import ru.kubsu.contracts.enums.service.client.OutboxStatus;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;
import ru.kubsu.contracts.enums.service.client.RequestStatus;
import ru.kubsu.contracts.messaging.service.client.ExternalRequestBatchMessage;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Обработка outbox-записей: публикация в RabbitMQ и обновление связанных статусов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublishProcessor {

    private static final String PUBLISH_LOCATION = "OutboxPublishProcessor.publishLockedMessage";

    /** Репозиторий outbox-сообщений. */
    private final OutboxMessageRepository outboxMessageRepository;

    /** Репозиторий внешних запросов. */
    private final ExternalRequestRepository externalRequestRepository;

    /** Репозиторий запросов по клиентам. */
    private final RequestRepository requestRepository;

    /** Шаблон публикации в RabbitMQ. */
    private final RabbitTemplate rabbitTemplate;

    /** Свойства outbox publisher. */
    private final OutboxProperties outboxProperties;

    /** JSON-сериализатор. */
    private final JsonMapper jsonMapper;

    /**
     * Публикует доступные outbox-записи со статусом NEW.
     */
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> messages = outboxMessageRepository.lockNextNewBatch(outboxProperties.getBatchSize());
        for (OutboxMessage message : messages) {
            publishLockedMessage(message);
        }
    }

    /**
     * Публикует одну заблокированную outbox-запись.
     *
     * @param message outbox-запись
     */
    private void publishLockedMessage(OutboxMessage message) {
        try {
            rabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), message.getPayload());
            markSent(message);
            applySuccessSideEffects(message);
        } catch (Exception exception) {
            handlePublishFailure(message, exception);
        }
    }

    /**
     * Помечает outbox-запись как успешно отправленную.
     *
     * @param message outbox-запись
     */
    private void markSent(OutboxMessage message) {
        message.setStatus(OutboxStatus.SENT);
        message.setSentAt(OffsetDateTime.now());
        message.setLastError(null);
        outboxMessageRepository.save(message);
    }

    /**
     * Обрабатывает ошибку публикации outbox-записи.
     *
     * @param message   outbox-запись
     * @param exception исключение
     */
    private void handlePublishFailure(OutboxMessage message, Exception exception) {
        int attempts = message.getAttempts() + 1;
        message.setAttempts(attempts);
        String errorMessage = OutboxErrorFormatter.format(
                PUBLISH_LOCATION, exception, outboxProperties.getMaxErrorLength());
        message.setLastError(errorMessage);

        if (attempts >= outboxProperties.getMaxAttempts()) {
            message.setStatus(OutboxStatus.FAILED);
            applyFailureSideEffects(message, errorMessage);
            log.warn("Outbox-сообщение {} переведено в FAILED после {} попыток", message.getId(), attempts);
        }

        outboxMessageRepository.save(message);
    }

    /**
     * Применяет побочные эффекты после успешной отправки outbox-записи.
     *
     * @param message outbox-запись
     */
    private void applySuccessSideEffects(OutboxMessage message) {
        if (message.getEventType() != OutboxEventType.EXTERNAL_REQUEST_SENT) {
            return;
        }

        ExternalRequestBatchMessage batchMessage = jsonMapper.readValue(
                message.getPayload(), ExternalRequestBatchMessage.class);
        requestRepository.updateStatusAndOutcomeByBatchRequestId(
                batchMessage.getBatchRequestId(), RequestStatus.SENT, RequestOutcome.PENDING);
        tryCompleteExternalRequest(message.getAggregateId());
    }

    /**
     * Применяет побочные эффекты после окончательной ошибки outbox-записи.
     *
     * @param message      outbox-запись
     * @param errorMessage детализация ошибки
     */
    private void applyFailureSideEffects(OutboxMessage message, String errorMessage) {
        if (message.getEventType() != OutboxEventType.EXTERNAL_REQUEST_SENT) {
            return;
        }

        requestRepository.updateStatusErrorAndOutcomeByExternalRequestId(
                message.getAggregateId(), RequestStatus.ERROR, errorMessage, RequestOutcome.ERROR);

        externalRequestRepository.findById(message.getAggregateId())
                .ifPresent(externalRequest -> externalRequest.setStatus(RequestStatus.ERROR));
    }

    /**
     * Переводит внешний запрос в SENT, если все outbox-записи агрегата отправлены.
     *
     * @param externalRequestId идентификатор внешнего запроса
     */
    private void tryCompleteExternalRequest(UUID externalRequestId) {
        boolean hasPending = outboxMessageRepository.existsByAggregateIdAndAggregateTypeAndStatusIn(
                externalRequestId,
                AggregateType.EXTERNAL_REQUEST,
                EnumSet.of(OutboxStatus.NEW));

        if (hasPending) {
            return;
        }

        boolean hasFailed = outboxMessageRepository.existsByAggregateIdAndAggregateTypeAndStatusIn(
                externalRequestId,
                AggregateType.EXTERNAL_REQUEST,
                EnumSet.of(OutboxStatus.FAILED));

        if (hasFailed) {
            return;
        }

        ExternalRequest externalRequest = externalRequestRepository.findById(externalRequestId)
                .orElse(null);
        if (externalRequest != null) {
            externalRequest.setStatus(RequestStatus.SENT);
        }
    }
}
