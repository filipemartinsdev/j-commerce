package com.products.application.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record AdminProductCategoryResponse (
        Long id,
        String name,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
}
