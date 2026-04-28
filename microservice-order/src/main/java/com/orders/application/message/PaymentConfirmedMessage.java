package com.orders.application.message;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentConfirmedMessage (
        UUID paymentId,
        UUID orderId,
        UUID userId,
        BigDecimal value
) {
}
