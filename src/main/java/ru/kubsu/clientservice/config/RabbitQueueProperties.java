package ru.kubsu.clientservice.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Свойства имён RabbitMQ exchanges, queues и routing keys.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.rabbit")
public class RabbitQueueProperties {

    /** Имена exchanges. */
    private Exchange exchange = new Exchange();

    /** Имена очередей. */
    private Queue queue = new Queue();

    /** Routing keys. */
    private RoutingKey routingKey = new RoutingKey();

    /**
     * Имена exchanges для RabbitMQ.
     */
    @Getter
    @Setter
    public static class Exchange {

        /** Exchange исходящих событий клиента. */
        @NotBlank
        private String clientEvents;

        /** Exchange исходящих запросов во внешние источники. */
        @NotBlank
        private String externalRequest;

        /** Exchange входящих ответов от внешних источников. */
        @NotBlank
        private String externalResponse;
    }

    /**
     * Имена очередей для RabbitMQ.
     */
    @Getter
    @Setter
    public static class Queue {

        /** Очередь запросов в ФНС. */
        @NotBlank
        private String externalRequestFns;

        /** Очередь запросов в ЕПГУ. */
        @NotBlank
        private String externalRequestEpgu;

        /** Очередь входящих ответов по запросам. */
        @NotBlank
        private String externalResponse;

        /** Очередь входящих рассылок без предварительного запроса. */
        @NotBlank
        private String externalBroadcast;
    }

    /**
     * Routing keys для RabbitMQ.
     */
    @Getter
    @Setter
    public static class RoutingKey {

        /** Routing key события создания клиента. */
        @NotBlank
        private String clientCreated;

        /** Routing key события обновления клиента. */
        @NotBlank
        private String clientUpdated;

        /** Routing key события удаления клиента. */
        @NotBlank
        private String clientDeleted;

        /** Routing key запроса в ФНС. */
        @NotBlank
        private String externalRequestFns;

        /** Routing key запроса в ЕПГУ. */
        @NotBlank
        private String externalRequestEpgu;

        /** Routing key ответа от ФНС. */
        @NotBlank
        private String externalResponseFns;

        /** Routing key ответа от ЕПГУ. */
        @NotBlank
        private String externalResponseEpgu;
    }
}
