package ru.kubsu.clientservice.query;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.util.StringUtils;
import ru.kubsu.clientservice.entity.Client;
import ru.kubsu.clientservice.entity.QClient;
import ru.kubsu.contracts.exception.service.client.ClientValidationException;

/**
 * Парсер ключа сортировки для QueryDSL-запросов по клиентам.
 */
public final class ClientSortParser {

    private static final QClient CLIENT = QClient.client;

    private ClientSortParser() {
    }

    /**
     * Преобразует ключ сортировки в {@link OrderSpecifier}.
     *
     * @param sortKey ключ сортировки в формате "field,asc|desc"
     * @return спецификатор сортировки QueryDSL
     */
    public static OrderSpecifier<?> parse(String sortKey) {
        if (!StringUtils.hasText(sortKey)) {
            return CLIENT.lastName.asc();
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
            case Client.Fields.lastName -> new OrderSpecifier<>(direction, CLIENT.lastName);
            case Client.Fields.firstName -> new OrderSpecifier<>(direction, CLIENT.firstName);
            case Client.Fields.patronymic -> new OrderSpecifier<>(direction, CLIENT.patronymic);
            case Client.Fields.birthDate -> new OrderSpecifier<>(direction, CLIENT.birthDate);
            case Client.Fields.actualDate -> new OrderSpecifier<>(direction, CLIENT.actualDate);
            case Client.Fields.status -> new OrderSpecifier<>(direction, CLIENT.status);
            case Client.Fields.identityDocumentSeries -> new OrderSpecifier<>(direction, CLIENT.identityDocumentSeries);
            case Client.Fields.identityDocumentNumber -> new OrderSpecifier<>(direction, CLIENT.identityDocumentNumber);
            case Client.Fields.itn -> new OrderSpecifier<>(direction, CLIENT.itn);
            case Client.Fields.insuranceNumber -> new OrderSpecifier<>(direction, CLIENT.insuranceNumber);
            case Client.Fields.residenceAddressName -> new OrderSpecifier<>(direction, CLIENT.residenceAddressName);
            default -> throw new ClientValidationException("Недопустимое поле сортировки: " + field);
        };
    }
}
