package com.orders.application.dto;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SalesOrderSummaryResponse (
        UUID id,
        String status,
        BigDecimal totalAmount,
        List<SalesOrderItemResponse> items,
        DeliveryAddressResponse deliveryAddress,
        ShippingResponse shipping,
        Instant createdAt
) {
    public static record ShippingResponse(
            UUID id,
            String status,
            Instant expectedDeliveryDate
    ){}
}

