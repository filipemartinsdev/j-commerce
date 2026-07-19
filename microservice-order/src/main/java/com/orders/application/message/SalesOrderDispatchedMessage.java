package com.orders.application.message;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderDispatchedMessage(
        UUID salesOrderId,
        UUID userId,
        UUID deliveryAddressId,
        BigDecimal totalAmount
) implements Serializable {
}
