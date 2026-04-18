package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.domain.entity.WishlistItem;
import com.products.domain.entity.WishlistItemProductSKUResume;
import org.springframework.stereotype.Component;

@Component
public class WishlistItemMapper {
    public WishlistItemResponse toResponse(WishlistItemProductSKUResume entity, Integer priceDiscountPercent) {
        return new WishlistItemResponse(
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                new ProductPriceCatalogueResponse(
                        entity.getOriginalPrice(),
                        entity.getCurrentPrice(),
                        priceDiscountPercent,
                        entity.getPriceTypeName()
                )
        );
    }
}
