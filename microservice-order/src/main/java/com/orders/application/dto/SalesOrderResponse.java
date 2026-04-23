package com.orders.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SalesOrderResponse(
        UUID id,
        Instant createdAt,
        String status,
        BigDecimal value,
        String shippingStatus
) {
}
