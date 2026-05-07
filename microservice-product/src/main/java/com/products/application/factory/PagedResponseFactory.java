package com.products.application.factory;

import com.products.application.dto.PagedResponse;
import com.products.application.exception.InvalidEntityMapperException;
import com.products.application.exception.NullResponsePageException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.function.Function;

@Component
public class PagedResponseFactory<O> {
    public <I> PagedResponse<O> fromPage(Page<I> page, Function<I, O> entityMapper){
        if (page == null)
            throw new NullResponsePageException("Page is null");

        if (entityMapper == null)
            throw new InvalidEntityMapperException("EntityMapper is null");

        if (page.getContent().isEmpty())
            return empty(page.getSize());

        return PagedResponse.<O>builder()
                .content(page.getContent().stream()
                        .map(entityMapper)
                        .toList()
                )
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private <I> PagedResponse<O> empty(int size) {
        return PagedResponse.<O>builder()
                .content(new ArrayList<>())
                .page(0)
                .size(size)
                .isLast(true)
                .totalElements(0L)
                .totalPages(0)
                .build();
    }
}
