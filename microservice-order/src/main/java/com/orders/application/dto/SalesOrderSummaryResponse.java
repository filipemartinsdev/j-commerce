package com.orders.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SalesOrderSummaryResponse (
        UUID id,
        String status,
        String shippingStatus,
        BigDecimal totalAmount,
        List<SalesOrderItemResponse> items,
        DeliveryAddressResponse deliveryAddress,
        Instant createdAt
) {
}

