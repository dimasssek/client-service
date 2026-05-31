package ru.kubsu.clientservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.kubsu.clientservice.response.BroadcastProcessor;
import ru.kubsu.contracts.messaging.service.client.ExternalBroadcastMessage;
import tools.jackson.databind.json.JsonMapper;

/**
 * Слушатель очереди рассылок от ведомств без предварительного запроса.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BroadcastListener {

    /** Обработчик рассылки. */
    private final BroadcastProcessor broadcastProcessor;

    /** JSON-сериализатор. */
    private final JsonMapper jsonMapper;

    /**
     * Принимает рассылку от ведомства.
     *
     * @param payload JSON-сообщение
     */
    @RabbitListener(queues = "${app.rabbit.queue.external-broadcast}")
    public void onBroadcast(String payload) {
        try {
            ExternalBroadcastMessage message = jsonMapper.readValue(payload, ExternalBroadcastMessage.class);
            broadcastProcessor.process(message);
        } catch (Exception exception) {
            log.error("Ошибка обработки рассылки ведомства at BroadcastListener.onBroadcast: {}",
                    exception.getMessage());
        }
    }
}
