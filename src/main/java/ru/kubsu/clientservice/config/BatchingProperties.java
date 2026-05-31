package ru.kubsu.clientservice.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Свойства разбиения клиентов на пакетные запросы.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.batching")
public class BatchingProperties {

    /** Максимальное количество записей в одной пачке. */
    @Min(1)
    private int size = 50;
}
