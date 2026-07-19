package com.notification.application.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record NotifyShippingDispatchedMessage(
        UUID userId,
        BigDecimal orderValue
) implements Serializable {
}
