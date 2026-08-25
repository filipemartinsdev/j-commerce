package com.products.application.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;

public record PriceUpdatedMessage(
        String sku,
        Optional<PriceMessage> basePrice,
        Optional<PriceMessage> currentPrice
) implements Serializable {

    public static record PriceMessage(
            String label,
            BigDecimal value
    ){}
}
