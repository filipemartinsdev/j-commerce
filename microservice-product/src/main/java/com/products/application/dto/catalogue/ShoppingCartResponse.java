package com.products.application.dto.catalogue;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingCartResponse(
        BigDecimal totalAmount,
        Integer itemsCount,
        List<Item> items
) {
    public static record Item (
            String SKU,
            String name,
            Integer units,
            BigDecimal price,
            BigDecimal amount
    ){}
}
