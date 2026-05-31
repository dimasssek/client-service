package ru.kubsu.clientservice.query;

import ru.kubsu.contracts.dto.service.client.ExternalRequestSummaryTo;
import ru.kubsu.contracts.dto.service.client.RequestTo;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;

import java.util.List;

/**
 * Построение сводки по итогам обработки внешнего запроса.
 */
public final class ExternalRequestSummaryBuilder {

    private ExternalRequestSummaryBuilder() {
    }

    /**
     * Формирует сводку по списку запросов.
     *
     * @param requests транспортные объекты запросов
     * @return сводка
     */
    public static ExternalRequestSummaryTo fromRequests(List<RequestTo> requests) {
        ExternalRequestSummaryTo summary = new ExternalRequestSummaryTo();
        for (RequestTo request : requests) {
            RequestOutcome outcome = request.getOutcome() != null ? request.getOutcome() : RequestOutcome.PENDING;
            switch (outcome) {
                case UPDATED -> summary.setUpdatedCount(summary.getUpdatedCount() + 1);
                case ACTUAL -> summary.setActualCount(summary.getActualCount() + 1);
                case NOT_FOUND -> summary.setNotFoundCount(summary.getNotFoundCount() + 1);
                case ERROR -> summary.setErrorCount(summary.getErrorCount() + 1);
                default -> summary.setPendingCount(summary.getPendingCount() + 1);
            }
        }
        return summary;
    }
}
