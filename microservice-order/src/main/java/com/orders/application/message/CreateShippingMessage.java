package com.orders.application.message;

import java.util.UUID;

public record CreateShippingMessage (
        UUID salesOrderId,
        UUID deliveryAddressId
) {
}
