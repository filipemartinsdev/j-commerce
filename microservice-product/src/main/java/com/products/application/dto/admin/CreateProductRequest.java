package com.products.application.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record CreateProductRequest(
        @NotEmpty String name,
        Optional<String> description,
        @NotNull Long categoryId
) {
}
