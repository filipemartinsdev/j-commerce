package com.products.application.dto.catalogue;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShoppingCartResponse(
        BigDecimal totalValue,
        List<ShoppingCart.Item> items
) implements Serializable {

}
