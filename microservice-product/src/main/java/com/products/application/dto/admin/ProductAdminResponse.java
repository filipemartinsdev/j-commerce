package com.products.application.dto.admin;

import com.products.application.dto.ProductCategoryResponse;

import java.time.Instant;
import java.util.UUID;

public record ProductAdminResponse (
        UUID id,
        String name,
        String description,
        ProductCategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) {
}
