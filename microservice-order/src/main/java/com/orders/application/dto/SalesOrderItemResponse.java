package com.orders.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemResponse(
    UUID productSKUId,
    String name,
    BigDecimal unitPrice,
    Integer units
) {
}
