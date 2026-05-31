package ru.kubsu.clientservice.query;

import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;
import ru.kubsu.contracts.dto.service.client.ExternalRequestQueryParams;

/**
 * Фабрика {@link Pageable} для поиска внешних запросов.
 */
public final class ExternalRequestPageableFactory {

    private static final int DEFAULT_PAGE_NO = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private ExternalRequestPageableFactory() {
    }

    /**
     * Формирует постраничный запрос с QueryDSL-сортировкой.
     *
     * @param params         параметры поиска
     * @param orderSpecifier спецификатор сортировки
     * @return постраничный запрос
     */
    public static Pageable from(ExternalRequestQueryParams params, OrderSpecifier<?> orderSpecifier) {
        int pageNo = params.getPageNo() != null ? params.getPageNo() : DEFAULT_PAGE_NO;
        int pageSize = params.getPageSize() != null ? params.getPageSize() : DEFAULT_PAGE_SIZE;
        return QPageRequest.of(pageNo, pageSize, orderSpecifier);
    }
}
