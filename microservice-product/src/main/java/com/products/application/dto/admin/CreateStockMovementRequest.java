package com.products.application.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Optional;

public record CreateStockMovementRequest(
        @NotEmpty String SKU,
        Optional<String> description,
        @NotNull Integer typeId,
        @NotNull Integer reasonId,
        @NotNull @Positive  Integer units
) {
}
