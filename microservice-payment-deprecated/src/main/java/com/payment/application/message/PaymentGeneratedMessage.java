package com.payment.application.message;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGeneratedMessage (
        UUID paymentId,
        UUID orderId,
        UUID userId,
        BigDecimal value
) {
}
