package com.products.application.message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RefundItemsMessage(
        UUID salesOrderId,
        UUID userId,
        List<OrderItem> items,
        BigDecimal value
) {
    public record OrderItem(
            String SKU,
            Integer units
    ) {
    }
}
