package ru.kubsu.clientservice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Feign-клиент для взаимодействия с application-service.
 * Методы будут дополняться по мере реализации бизнес-логики.
 */
@FeignClient(
        name = "application-service",
        url = "${app.feign.application-service.url}"
)
public interface ApplicationServiceClient {

    /**
     * Заглушка уведомления application-service об обновлении клиента.
     * Реальная интеграция будет реализована позже.
     */
    @PostMapping("/api/v1/internal/clients/updated")
    void notifyClientUpdated();
}
