package com.products.application.dto.admin;

import java.util.UUID;

public record CreateStockEntryRequest(
        UUID productSKUId,
        Integer units
) {
}
