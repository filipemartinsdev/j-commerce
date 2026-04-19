package com.products.application.dto.catalogue;

import java.math.BigDecimal;
import java.util.UUID;

public record ShoppingCartItemResponse (
        UUID id,
        UUID productSKUId,
        String productSKUName,
        Integer units,
        BigDecimal originalPrice,
        BigDecimal currentPrice,
        Integer discountPercent
) {
}
