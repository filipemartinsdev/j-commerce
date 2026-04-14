package com.products.application.dto.catalogue;

import com.products.application.dto.StockStatus;

import java.util.UUID;

public record ProductSKUSummaryCatalogueResponse(
        UUID id,
        String SKU,
        String name,
        StockStatus stockStatus,
        ProductPriceCatalogueResponse price
) {
}
