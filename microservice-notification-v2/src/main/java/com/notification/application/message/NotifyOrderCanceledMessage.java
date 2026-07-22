package com.notification.application.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record NotifyOrderCanceledMessage(
        UUID salesOrderId,
        UUID userId,
        List<OrderItem> items,
        BigDecimal totalAmount

) implements Serializable {
    public static record OrderItem(
            UUID productSkuId,
            Integer units
    ) implements Serializable {
    }
}
