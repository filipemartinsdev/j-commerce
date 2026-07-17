package com.products.application.dto.catalogue;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShoppingCart(
        List<Item> items
) implements Serializable {

    public static record Item (
            UUID productSKUId,
            UUID productId,
            String productSKUName,
            int units,
            BigDecimal price
    ) implements Serializable {}
}
