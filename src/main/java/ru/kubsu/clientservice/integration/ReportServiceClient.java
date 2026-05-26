package ru.kubsu.clientservice.integration;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign-клиент для взаимодействия с report-service.
 * Методы будут дополняться по мере реализации бизнес-логики.
 */
@FeignClient(
        name = "report-service",
        url = "${app.feign.report-service.url}"
)
public interface ReportServiceClient {
}
