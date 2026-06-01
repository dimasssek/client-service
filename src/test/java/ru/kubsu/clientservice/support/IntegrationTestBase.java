package ru.kubsu.clientservice.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Базовый класс интеграционных тестов с Testcontainers Postgres и RabbitMQ.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    /** Контейнер PostgreSQL alpine для интеграционных тестов. */
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("client-service")
            .withUsername("user")
            .withPassword("password");

    /** Контейнер RabbitMQ для интеграционных тестов. */
    protected static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3-management-alpine");

    static {
        POSTGRES.start();
        RABBIT.start();
    }

    /** MockMvc для HTTP-запросов в тестах. */
    @Autowired
    protected MockMvc mockMvc;

    /** JsonMapper для сериализации/десериализации JSON в тестах. */
    @Autowired
    protected JsonMapper jsonMapper;

    /**
     * Подставляет параметры подключения к БД и RabbitMQ из Testcontainers.
     *
     * @param registry реестр динамических свойств Spring
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }
}
