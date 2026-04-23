package com.orders.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedResponse<T> (
        Integer page,
        Integer size,
        Integer totalPages,
        Long totalElements,
        Boolean isLast,
        List<T> content
){
}
