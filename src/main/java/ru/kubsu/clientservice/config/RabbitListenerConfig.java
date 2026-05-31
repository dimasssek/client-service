package ru.kubsu.clientservice.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация RabbitMQ listeners.
 */
@Configuration
@EnableRabbit
public class RabbitListenerConfig {
}
