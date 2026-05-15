package com.orders.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ShippingResponse (
        UUID id,
        String status,
        UUID salesOrderId,
        UUID deliveryAddressId,
        Instant expectedDeliveryDate,
        UUID driverId,
        Instant createdAt
) {
}
