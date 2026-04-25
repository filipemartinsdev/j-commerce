package com.orders.application.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderMessage(
        UUID userId,
        List<OrderItem> items,
        UUID deliveryAddressId
) implements Serializable {
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderItem implements Serializable {
        private UUID productSKUId;
        private String name;
        private Integer units;
        private BigDecimal unitPrice;
    }
}
