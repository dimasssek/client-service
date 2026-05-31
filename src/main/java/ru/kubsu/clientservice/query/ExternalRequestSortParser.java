package ru.kubsu.clientservice.query;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.util.StringUtils;
import ru.kubsu.clientservice.entity.ExternalRequest;
import ru.kubsu.clientservice.entity.QExternalRequest;
import ru.kubsu.contracts.exception.service.client.ClientValidationException;

/**
 * Парсер ключа сортировки для QueryDSL-запросов по внешним запросам.
 */
public final class ExternalRequestSortParser {

    private static final QExternalRequest EXTERNAL_REQUEST = QExternalRequest.externalRequest;

    private ExternalRequestSortParser() {
    }

    /**
     * Преобразует ключ сортировки в {@link OrderSpecifier}.
     *
     * @param sortKey ключ сортировки в формате "field,asc|desc"
     * @return спецификатор сортировки QueryDSL
     */
    public static OrderSpecifier<?> parse(String sortKey) {
        if (!StringUtils.hasText(sortKey)) {
            return EXTERNAL_REQUEST.created.desc();
        }

        String[] parts = sortKey.split(",");
        if (parts.length == 0 || !StringUtils.hasText(parts[0])) {
            throw new ClientValidationException("Некорректный ключ сортировки: " + sortKey);
        }

        String field = parts[0].trim();
        Order direction = Order.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            direction = Order.DESC;
        }

        return switch (field) {
            case ExternalRequest.Fields.letterNumber -> new OrderSpecifier<>(direction, EXTERNAL_REQUEST.letterNumber);
            case ExternalRequest.Fields.letterDate -> new OrderSpecifier<>(direction, EXTERNAL_REQUEST.letterDate);
            case ExternalRequest.Fields.sourceType -> new OrderSpecifier<>(direction, EXTERNAL_REQUEST.sourceType);
            case ExternalRequest.Fields.status -> new OrderSpecifier<>(direction, EXTERNAL_REQUEST.status);
            case ExternalRequest.Fields.initiatorLogin -> new OrderSpecifier<>(direction, EXTERNAL_REQUEST.initiatorLogin);
            case ExternalRequest.Fields.created -> new OrderSpecifier<>(direction, EXTERNAL_REQUEST.created);
            default -> throw new ClientValidationException("Недопустимое поле сортировки: " + field);
        };
    }
}
