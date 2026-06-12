package com.notification.application.message;

import java.math.BigDecimal;
import java.util.UUID;

public record NotifyPaymentGeneratedMessage (
        UUID paymentId,
        UUID orderId,
        UUID userId,
        BigDecimal value
) {
}
