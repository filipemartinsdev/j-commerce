package com.notification.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedResponse<T> (
        long page,
        long size,
        boolean isLast,
        long totalElements,
        long totalPages,
        List<T> content
){
}
