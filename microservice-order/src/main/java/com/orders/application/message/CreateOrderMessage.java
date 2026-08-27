package com.orders.application.message;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderMessage(
        UUID userId,
        List<OrderItem> items,
        UUID deliveryAddressId
) implements Serializable {
    public static record OrderItem(
            String sku,
            String name,
            Integer units,
            BigDecimal unitPrice
    ) implements Serializable {
    }
}
