package com.products.application.dto.catalogue;

import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        UUID productSKUId,
        String productSKUName,
        ProductPriceCatalogueResponse price
) {
}
