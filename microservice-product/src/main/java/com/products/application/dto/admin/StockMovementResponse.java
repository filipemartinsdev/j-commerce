package com.products.application.dto.admin;

import com.products.domain.entity.StockMovementType;

import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse (
        UUID id,
        UUID productSKUId,
        String productSKUName,
        Integer units,
        StockMovementTypeResponse type,
        Instant createdAt,
        UUID createdBy
) {
}
