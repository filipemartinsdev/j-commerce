package com.products.application.dto.catalogue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateShoppingCartItemRequest(
        @NotNull String SKU,
        @NotNull @Positive Integer units
) {
}
