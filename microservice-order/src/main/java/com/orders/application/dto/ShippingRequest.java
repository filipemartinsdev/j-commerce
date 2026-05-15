package com.orders.application.dto;

import java.util.UUID;

public record ShippingRequest (
        UUID salesOrderId,
        UUID deliveryAddressId
) {
}
