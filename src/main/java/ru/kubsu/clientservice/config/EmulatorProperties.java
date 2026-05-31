package ru.kubsu.clientservice.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Свойства эмулятора внешних ведомств.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.emulator")
public class EmulatorProperties {

    /** Признак включения эмулятора. */
    private boolean enabled = false;

    /** Минимальная задержка ответа в миллисекундах. */
    @Min(0)
    private long minDelayMs = 10_000;

    /** Максимальная задержка ответа в миллисекундах. */
    @Min(0)
    private long maxDelayMs = 20_000;
}
