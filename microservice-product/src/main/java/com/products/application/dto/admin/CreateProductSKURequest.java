package com.products.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

public record CreateProductSKURequest (
        @NotNull UUID productId,
        @NotBlank String name,
        @NotBlank String SKU
) {
}
