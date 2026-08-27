package com.orders.application.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SalesOrderCanceledMessage(
        UUID salesOrderId,
        UUID userId,
        List<OrderItem> items,
        BigDecimal totalAmount

) implements Serializable {
    public static record OrderItem(
            String sku,
            Integer units
    ) implements Serializable {
    }
}
