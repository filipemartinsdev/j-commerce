package com.products.application.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record ProductStockResponse (
        UUID id,
        UUID productId,
        UUID productSKUId,
        String name,
        String SKU,
        Integer units,
        Instant updatedAt
) {

}
