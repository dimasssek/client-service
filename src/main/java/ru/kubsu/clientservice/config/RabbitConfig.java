package ru.kubsu.clientservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация RabbitMQ: exchanges, queues и bindings.
 */
@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    /** Свойства имён RabbitMQ. */
    private final RabbitQueueProperties rabbitQueueProperties;

    /**
     * Exchange исходящих событий клиента.
     *
     * @return topic exchange
     */
    @Bean
    public TopicExchange clientEventsExchange() {
        return new TopicExchange(rabbitQueueProperties.getExchange().getClientEvents());
    }

    /**
     * Exchange исходящих запросов во внешние источники.
     *
     * @return topic exchange
     */
    @Bean
    public TopicExchange externalRequestExchange() {
        return new TopicExchange(rabbitQueueProperties.getExchange().getExternalRequest());
    }

    /**
     * Exchange входящих ответов от внешних источников.
     *
     * @return topic exchange
     */
    @Bean
    public TopicExchange externalResponseExchange() {
        return new TopicExchange(rabbitQueueProperties.getExchange().getExternalResponse());
    }

    /**
     * Очередь запросов в ФНС.
     *
     * @return durable queue
     */
    @Bean
    public Queue externalRequestFnsQueue() {
        return new Queue(rabbitQueueProperties.getQueue().getExternalRequestFns(), true);
    }

    /**
     * Очередь запросов в ЕПГУ.
     *
     * @return durable queue
     */
    @Bean
    public Queue externalRequestEpguQueue() {
        return new Queue(rabbitQueueProperties.getQueue().getExternalRequestEpgu(), true);
    }

    /**
     * Очередь входящих ответов по запросам.
     *
     * @return durable queue
     */
    @Bean
    public Queue externalResponseQueue() {
        return new Queue(rabbitQueueProperties.getQueue().getExternalResponse(), true);
    }

    /**
     * Очередь входящих рассылок без предварительного запроса.
     *
     * @return durable queue
     */
    @Bean
    public Queue externalBroadcastQueue() {
        return new Queue(rabbitQueueProperties.getQueue().getExternalBroadcast(), true);
    }

    /**
     * Binding очереди запросов в ФНС.
     *
     * @return binding
     */
    @Bean
    public Binding externalRequestFnsBinding() {
        return BindingBuilder
                .bind(externalRequestFnsQueue())
                .to(externalRequestExchange())
                .with(rabbitQueueProperties.getRoutingKey().getExternalRequestFns());
    }

    /**
     * Binding очереди запросов в ЕПГУ.
     *
     * @return binding
     */
    @Bean
    public Binding externalRequestEpguBinding() {
        return BindingBuilder
                .bind(externalRequestEpguQueue())
                .to(externalRequestExchange())
                .with(rabbitQueueProperties.getRoutingKey().getExternalRequestEpgu());
    }

    /**
     * Binding очереди входящих ответов по запросам (ФНС).
     *
     * @return binding
     */
    @Bean
    public Binding externalResponseFnsBinding() {
        return BindingBuilder
                .bind(externalResponseQueue())
                .to(externalResponseExchange())
                .with(rabbitQueueProperties.getRoutingKey().getExternalResponseFns());
    }

    /**
     * Binding очереди входящих ответов по запросам (ЕПГУ).
     *
     * @return binding
     */
    @Bean
    public Binding externalResponseEpguBinding() {
        return BindingBuilder
                .bind(externalResponseQueue())
                .to(externalResponseExchange())
                .with(rabbitQueueProperties.getRoutingKey().getExternalResponseEpgu());
    }

    /**
     * Binding очереди входящих рассылок (ФНС).
     *
     * @return binding
     */
    @Bean
    public Binding externalBroadcastFnsBinding() {
        return BindingBuilder
                .bind(externalBroadcastQueue())
                .to(externalResponseExchange())
                .with(rabbitQueueProperties.getRoutingKey().getExternalResponseFns());
    }

    /**
     * Binding очереди входящих рассылок (ЕПГУ).
     *
     * @return binding
     */
    @Bean
    public Binding externalBroadcastEpguBinding() {
        return BindingBuilder
                .bind(externalBroadcastQueue())
                .to(externalResponseExchange())
                .with(rabbitQueueProperties.getRoutingKey().getExternalResponseEpgu());
    }
}
