package com.orders.application.message;

import java.math.BigDecimal;
import java.util.UUID;

public record NotifyShippingDispatchedMessage(
        UUID userId,
        BigDecimal orderValue
) {
}
