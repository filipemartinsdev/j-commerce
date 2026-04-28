package com.notification.application.message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record NotifyOrderCancelledMessage(
        UUID salesOrderId,
        UUID userId,
        List<OrderItem> items,
        BigDecimal value
) {
    public record OrderItem(
            UUID productSKUId,
            Integer units
    ) {
    }
}