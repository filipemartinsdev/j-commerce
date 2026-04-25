package com.orders.application.message;

import java.math.BigDecimal;
import java.util.UUID;

public record GeneratePaymentMessage (
        UUID orderId,
        UUID userId,
        BigDecimal totalAmount
) {
}
