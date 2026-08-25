package com.products.application.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

public record UpdateProductSKURequest(
        Optional<String> name,
        Optional<List<Attribute>> attributes
) {
    public static record Attribute (
            @NotEmpty
            String name,

            @NotEmpty
            String value
    ) {}
}
