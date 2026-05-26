package ru.kubsu.clientservice.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Feign-клиентов к смежным микросервисам.
 */
@Configuration
@EnableFeignClients(basePackages = "ru.kubsu.clientservice.integration")
@ConfigurationPropertiesScan(basePackages = "ru.kubsu.clientservice.config")
public class FeignConfig {
}
