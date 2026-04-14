package com.products.application.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record CreateProductSKUPrice (
        @NotNull UUID productSKUId,
        @NotNull BigDecimal price,
        @NotNull Integer priceTypeId,
        Optional<Instant> startAt,
        Optional<Instant> endAt
) {
}
