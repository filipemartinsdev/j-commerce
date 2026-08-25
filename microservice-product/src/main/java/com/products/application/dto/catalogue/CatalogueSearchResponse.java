package com.products.application.dto.catalogue;

import java.util.List;

public record CatalogueSearchResponse(
        String similarQuery,
        List<ProductCatalogueResponse> products
) {
}
