package ru.kubsu.clientservice.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Свойства outbox publisher.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.outbox")
public class OutboxProperties {

    /** Интервал опроса outbox в миллисекундах. */
    @Min(1)
    private long pollIntervalMs = 5000;

    /** Размер пачки outbox-записей за один цикл. */
    @Min(1)
    private int batchSize = 50;

    /** Максимальное количество попыток отправки. */
    @Min(1)
    private int maxAttempts = 5;

    /** Максимальная длина текста ошибки в varchar-поле. */
    @Min(100)
    private int maxErrorLength = 2000;
}
