package com.products.application.dto.catalogue;

import java.util.UUID;

public record WishlistItemResponse(
        UUID productSKUId,
        String productSKUName,
        ProductPriceCatalogueResponse price
) {
}
