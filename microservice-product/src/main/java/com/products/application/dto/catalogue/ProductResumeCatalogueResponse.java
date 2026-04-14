package com.products.application.dto.catalogue;

import com.products.application.dto.ProductCategoryResponse;

import java.util.UUID;

public record ProductResumeCatalogueResponse (
        UUID productId,
        String name,
        ProductCategoryResponse category,
        ProductPriceCatalogueResponse price
) {
}
