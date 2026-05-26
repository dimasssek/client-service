package ru.kubsu.clientservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Конфигурация планировщика для outbox publisher.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
