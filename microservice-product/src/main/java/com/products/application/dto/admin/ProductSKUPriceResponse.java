package com.products.application.dto.admin;

import com.products.application.dto.PriceTypeResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductSKUPriceResponse (
        UUID id,
        UUID productSKUId,
        String productSKUName,
        BigDecimal price,
        PriceTypeResponse type,
        Instant startAt,
        Instant endAt,
        Instant createdAt
) {
}
