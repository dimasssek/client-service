package ru.kubsu.clientservice.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.StringUtils;
import ru.kubsu.clientservice.entity.QExternalRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestQueryParams;
import ru.kubsu.contracts.enums.service.client.RequestStatus;

import java.util.Set;

/**
 * Построитель QueryDSL-предикатов для поиска внешних запросов.
 */
public final class ExternalRequestPredicateBuilder {

    private static final QExternalRequest EXTERNAL_REQUEST = QExternalRequest.externalRequest;

    private ExternalRequestPredicateBuilder() {
    }

    /**
     * Формирует предикат по параметрам поиска внешних запросов.
     *
     * @param params параметры поиска
     * @return QueryDSL-предикат
     */
    public static Predicate from(ExternalRequestQueryParams params) {
        BooleanBuilder builder = new BooleanBuilder();

        applyStatusFilter(params.getStatuses(), builder);

        if (StringUtils.hasText(params.getLetterNumber())) {
            builder.and(EXTERNAL_REQUEST.letterNumber.containsIgnoreCase(params.getLetterNumber()));
        }
        if (params.getLetterDate() != null) {
            builder.and(EXTERNAL_REQUEST.letterDate.eq(params.getLetterDate()));
        }
        if (params.getSourceType() != null) {
            builder.and(EXTERNAL_REQUEST.sourceType.eq(params.getSourceType()));
        }
        if (StringUtils.hasText(params.getInitiatorLogin())) {
            builder.and(EXTERNAL_REQUEST.initiatorLogin.containsIgnoreCase(params.getInitiatorLogin()));
        }

        return builder;
    }

    /**
     * Добавляет фильтр по статусам внешнего запроса.
     *
     * @param statuses набор статусов из запроса
     * @param builder  построитель предиката
     */
    private static void applyStatusFilter(Set<RequestStatus> statuses, BooleanBuilder builder) {
        if (statuses == null || statuses.isEmpty()) {
            return;
        }
        BooleanExpression statusExpression = null;
        for (RequestStatus status : statuses) {
            BooleanExpression current = EXTERNAL_REQUEST.status.eq(status);
            statusExpression = statusExpression == null ? current : statusExpression.or(current);
        }
        builder.and(statusExpression);
    }
}
