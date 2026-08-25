package com.products.application.dto.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateProductSKURequest(
        @NotEmpty
        String SKU,

        @NotEmpty
        String name,

        List<Attribute> attributes
) {
    public static record Attribute (
            @NotEmpty
            String name,

            @NotEmpty
            String value
    ) {}
}
