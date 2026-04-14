package com.products.application.dto.catalogue;

import java.math.BigDecimal;

public record ProductPriceCatalogueResponse(
        BigDecimal original,
        BigDecimal current,
        Integer discountPercent,
        String type
) {
}
