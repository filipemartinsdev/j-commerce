package com.products.application.dto.admin;

import java.util.Optional;

public record UpdateProductRequest (
        Optional<String> name,
        Optional<String> description,
        Optional<Integer> categoryId
) {
}
