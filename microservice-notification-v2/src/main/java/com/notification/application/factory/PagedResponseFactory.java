package com.notification.application.factory;

import com.notification.application.dto.PagedResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Function;

@ApplicationScoped
public class PagedResponseFactory {
    public <I, O> PagedResponse<O> create(PanacheQuery<I> query, Function<I, O> entityMapper){
        return new PagedResponse<O>(
                query.page().index,
                query.page().size,
                !query.hasNextPage(),
                query.count(),
                query.pageCount(),
                query.stream()
                        .map(entityMapper)
                        .toList()
        );
    }
}
