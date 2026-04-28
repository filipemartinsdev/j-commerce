package com.orders.application.message;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

public record SalesOrderCancelledMessage (
        UUID salesOrderId,
        UUID userId,
        List<OrderItem> items,
        BigDecimal value
) {
    public record OrderItem (
            UUID productSKUId,
            Integer units
    ) {}
}
