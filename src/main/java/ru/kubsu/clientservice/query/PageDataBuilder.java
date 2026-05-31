package ru.kubsu.clientservice.query;

import org.springframework.data.domain.Page;
import ru.kubsu.contracts.dto.common.PageData;
import ru.kubsu.contracts.dto.common.PageMetaData;

import java.util.List;
import java.util.function.Function;

/**
 * Построитель {@link PageData} на основе Spring {@link Page}.
 */
public final class PageDataBuilder {

    private PageDataBuilder() {
    }

    /**
     * Преобразует страницу сущностей в {@link PageData} транспортных объектов.
     *
     * @param page   страница сущностей
     * @param mapper функция маппинга
     * @param <E>    тип сущности
     * @param <T>    тип транспортного объекта
     * @return страница транспортных объектов
     */
    public static <E, T> PageData<T> from(Page<E> page, Function<E, T> mapper) {
        PageMetaData metaData = new PageMetaData()
                .setTotalElements(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .setNumber(page.getNumber())
                .setSize(page.getSize());

        List<T> content = page.getContent().stream()
                .map(mapper)
                .toList();

        return new PageData<T>()
                .setContent(content)
                .setMetaData(metaData);
    }
}
