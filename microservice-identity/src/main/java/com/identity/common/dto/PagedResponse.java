package com.identity.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
