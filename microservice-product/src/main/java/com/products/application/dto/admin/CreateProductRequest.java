package com.products.application.dto.admin;

import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record CreateProductRequest (
        @NotBlank String name,
        Optional<String> description,
        Integer categoryId
) {
}
