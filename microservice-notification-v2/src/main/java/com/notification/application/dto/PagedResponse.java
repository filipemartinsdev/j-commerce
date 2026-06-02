package com.notification.application.dto;

import java.util.List;

public record PagedResponse<T> (
        long page,
        long size,
        boolean isLast,
        long totalElements,
        long totalPages,
        List<T> content
){
}
