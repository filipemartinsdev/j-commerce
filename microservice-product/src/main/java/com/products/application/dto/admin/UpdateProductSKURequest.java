package com.products.application.dto.admin;

import java.util.Optional;

public record UpdateProductSKURequest(
        Optional<String> name,
        Optional<String> SKU
) {
}
