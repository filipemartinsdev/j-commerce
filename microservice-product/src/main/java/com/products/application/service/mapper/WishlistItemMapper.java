package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.service.ProductDiscountCalculator;
import com.products.domain.entity.WishlistItemSummaryView;
import org.springframework.stereotype.Component;

@Component
public class WishlistItemMapper {
    private final ProductDiscountCalculator productDiscountCalculator;

    public WishlistItemMapper(ProductDiscountCalculator productDiscountCalculator) {
        this.productDiscountCalculator = productDiscountCalculator;
    }

    public WishlistItemResponse toResponse(WishlistItemSummaryView entity) {
        return new WishlistItemResponse(
                entity.getId(),
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                new ProductPriceCatalogueResponse(
                        entity.getOriginalPrice(),
                        entity.getCurrentPrice(),
                        productDiscountCalculator.getDiscountPercent(entity.getOriginalPrice(), entity.getCurrentPrice()),
                        entity.getPriceTypeName()
                )
        );
    }
}
