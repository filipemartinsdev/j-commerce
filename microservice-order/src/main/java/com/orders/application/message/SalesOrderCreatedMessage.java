package com.orders.application.message;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderCreatedMessage(
        UUID id,
        UUID userId,
        UUID deliveryAddressId,
        BigDecimal totalAmount
) {
}
