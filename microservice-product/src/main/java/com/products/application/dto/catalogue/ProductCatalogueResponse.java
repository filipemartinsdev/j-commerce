package com.products.application.dto.catalogue;

import com.products.application.dto.ProductCategoryResponse;

import java.util.List;
import java.util.UUID;

public record ProductCatalogueResponse(
        UUID id,
        String name,
        String description,
        ProductCategoryResponse category,
        List<ProductSKUCatalogueResponse> SKUs
) {
}
