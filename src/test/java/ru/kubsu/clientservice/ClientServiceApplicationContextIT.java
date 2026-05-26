package ru.kubsu.clientservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.kubsu.clientservice.config.RabbitQueueProperties;
import ru.kubsu.clientservice.support.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke-тест инфраструктурного каркаса client-service.
 */
class ClientServiceApplicationContextIT extends IntegrationTestBase {

    /** Spring-контекст приложения. */
    @Autowired
    private ApplicationContext applicationContext;

    /** Свойства имён RabbitMQ для проверки конфигурации. */
    @Autowired
    private RabbitQueueProperties rabbitQueueProperties;

    /**
     * Проверяет, что Spring-контекст поднимается и Liquibase накатывает миграции.
     */
    @Test
    void should_loadSpringContextAndApplyMigrations() {
        // Контекст должен успешно инициализироваться вместе с Liquibase и Testcontainers Postgres
        assertNotNull(applicationContext);
        assertNotNull(mockMvc);
        assertNotNull(jsonMapper);
    }

    /**
     * Проверяет, что имена очередей и exchanges загружаются из application.yml.
     */
    @Test
    void should_loadRabbitQueuePropertiesFromConfiguration() {
        // Имена должны совпадать с дефолтными значениями из application.yml
        assertThat(rabbitQueueProperties.getExchange().getClientEvents())
                .isEqualTo("client.events.exchange");
        assertThat(rabbitQueueProperties.getQueue().getExternalRequestFns())
                .isEqualTo("client.external-request.fns.queue");
        assertThat(rabbitQueueProperties.getRoutingKey().getClientUpdated())
                .isEqualTo("client.updated");
    }
}
