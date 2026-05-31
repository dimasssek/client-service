package ru.kubsu.clientservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.kubsu.clientservice.response.ExternalResponseProcessor;
import ru.kubsu.contracts.messaging.service.client.ExternalResponseBatchMessage;
import tools.jackson.databind.json.JsonMapper;

/**
 * Слушатель очереди ответов ведомств по исходящим запросам.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalResponseListener {

    /** Обработчик пачки ответа. */
    private final ExternalResponseProcessor externalResponseProcessor;

    /** JSON-сериализатор. */
    private final JsonMapper jsonMapper;

    /**
     * Принимает пачку ответа от ведомства.
     *
     * @param payload JSON-сообщение
     */
    @RabbitListener(queues = "${app.rabbit.queue.external-response}")
    public void onResponse(String payload) {
        try {
            ExternalResponseBatchMessage message = jsonMapper.readValue(payload, ExternalResponseBatchMessage.class);
            externalResponseProcessor.process(message);
        } catch (Exception exception) {
            log.error("Ошибка обработки ответа ведомства at ExternalResponseListener.onResponse: {}",
                    exception.getMessage());
        }
    }
}
