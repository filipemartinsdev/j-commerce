package com.products.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateWishlistItemRequest(
        @NotNull UUID productSKUId,
        @Positive Integer units
) {
}
