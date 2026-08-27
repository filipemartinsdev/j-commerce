package com.orders.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemResponse(
    String sku,
    String name,
    BigDecimal unitPrice,
    Integer units
) {
}
