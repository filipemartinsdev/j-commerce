package com.products.application.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record ProductSKUAdminResponse(
    UUID id,
    UUID productId,
    String SKU,
    String name,
    Instant createdAt,
    Instant updatedAt,
    Boolean isActive
) {
}
