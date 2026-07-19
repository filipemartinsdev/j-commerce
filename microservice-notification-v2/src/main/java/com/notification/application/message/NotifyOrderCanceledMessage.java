package com.notification.application.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record NotifyOrderCanceledMessage(
        UUID paymentId,
        UUID orderId,
        UUID userId,
        BigDecimal amount
) implements Serializable {
}
