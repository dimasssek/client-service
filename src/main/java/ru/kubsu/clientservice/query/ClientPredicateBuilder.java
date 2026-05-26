package ru.kubsu.clientservice.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.StringUtils;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.QClient;
import ru.kubsu.contracts.dto.service.client.ClientQueryParams;
import ru.kubsu.contracts.enums.service.client.ClientStatus;

import java.util.Set;

/**
 * Построитель QueryDSL-предикатов для поиска клиентов.
 */
public final class ClientPredicateBuilder {

    private static final QClient CLIENT = QClient.client;

    private ClientPredicateBuilder() {
    }

    /**
     * Формирует предикат по параметрам поиска клиентов.
     *
     * @param params параметры поиска
     * @return QueryDSL-предикат
     */
    public static Predicate from(ClientQueryParams params) {
        BooleanBuilder builder = new BooleanBuilder();

        applyStatusFilter(params.getStatuses(), builder);

        if (StringUtils.hasText(params.getLastName())) {
            builder.and(CLIENT.lastName.containsIgnoreCase(params.getLastName()));
        }
        if (StringUtils.hasText(params.getFirstName())) {
            builder.and(CLIENT.firstName.containsIgnoreCase(params.getFirstName()));
        }
        if (StringUtils.hasText(params.getPatronymic())) {
            builder.and(CLIENT.patronymic.containsIgnoreCase(params.getPatronymic()));
        }
        if (StringUtils.hasText(params.getResidenceAddressName())) {
            builder.and(CLIENT.residenceAddressName.containsIgnoreCase(params.getResidenceAddressName()));
        }
        if (params.getBirthDate() != null) {
            builder.and(CLIENT.birthDate.eq(params.getBirthDate()));
        }
        if (params.getGender() != null) {
            builder.and(CLIENT.gender.eq(params.getGender()));
        }
        if (StringUtils.hasText(params.getIdentityDocumentSeries())) {
            builder.and(CLIENT.identityDocumentSeries.eq(params.getIdentityDocumentSeries()));
        }
        if (StringUtils.hasText(params.getIdentityDocumentNumber())) {
            builder.and(CLIENT.identityDocumentNumber.eq(params.getIdentityDocumentNumber()));
        }
        if (params.getIdentityDocumentIssueDate() != null) {
            builder.and(CLIENT.identityDocumentIssueDate.eq(params.getIdentityDocumentIssueDate()));
        }
        if (StringUtils.hasText(params.getItn())) {
            builder.and(CLIENT.itn.eq(params.getItn()));
        }
        if (StringUtils.hasText(params.getInsuranceNumber())) {
            builder.and(CLIENT.insuranceNumber.eq(params.getInsuranceNumber()));
        }

        return builder;
    }

    /**
     * Добавляет фильтр по статусам клиента.
     *
     * @param statuses набор статусов из запроса
     * @param builder  построитель предиката
     */
    private static void applyStatusFilter(Set<ClientStatus> statuses, BooleanBuilder builder) {
        if (statuses == null || statuses.isEmpty()) {
            builder.and(CLIENT.status.eq(ClientStatus.ACTUAL));
            return;
        }
        BooleanExpression statusExpression = null;
        for (ClientStatus status : statuses) {
            BooleanExpression current = CLIENT.status.eq(status);
            statusExpression = statusExpression == null ? current : statusExpression.or(current);
        }
        builder.and(statusExpression);
    }
}
