package com.products.application.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public record UpdateProductSKUPriceRequest (
        Optional<BigDecimal> price,
        Optional<Integer> priceType,
        Optional<Instant> startAt,
        Optional<Instant> endAt
) {
}
