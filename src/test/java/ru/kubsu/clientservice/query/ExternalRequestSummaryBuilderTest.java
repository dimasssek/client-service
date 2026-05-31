package ru.kubsu.clientservice.query;

import org.junit.jupiter.api.Test;
import ru.kubsu.contracts.dto.service.client.ExternalRequestSummaryTo;
import ru.kubsu.contracts.dto.service.client.RequestTo;
import ru.kubsu.contracts.enums.service.client.RequestOutcome;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты построения сводки по внешнему запросу.
 */
class ExternalRequestSummaryBuilderTest {

    /**
     * Проверяет подсчёт итогов по запросам.
     */
    @Test
    void should_countOutcomes() {
        ExternalRequestSummaryTo summary = ExternalRequestSummaryBuilder.fromRequests(List.of(
                new RequestTo().setOutcome(RequestOutcome.UPDATED),
                new RequestTo().setOutcome(RequestOutcome.ACTUAL),
                new RequestTo().setOutcome(RequestOutcome.NOT_FOUND),
                new RequestTo().setOutcome(RequestOutcome.ERROR),
                new RequestTo().setOutcome(RequestOutcome.PENDING),
                new RequestTo()
        ));

        assertThat(summary.getUpdatedCount()).isEqualTo(1);
        assertThat(summary.getActualCount()).isEqualTo(1);
        assertThat(summary.getNotFoundCount()).isEqualTo(1);
        assertThat(summary.getErrorCount()).isEqualTo(1);
        assertThat(summary.getPendingCount()).isEqualTo(2);
    }
}
