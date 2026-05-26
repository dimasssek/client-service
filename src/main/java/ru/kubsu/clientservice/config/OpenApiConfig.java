package ru.kubsu.clientservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI / Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Описание OpenAPI для client-service.
     *
     * @return конфигурация OpenAPI
     */
    @Bean
    public OpenAPI clientServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("client-service API")
                        .description("REST API микросервиса учёта клиентов банка")
                        .version("v1"));
    }
}
