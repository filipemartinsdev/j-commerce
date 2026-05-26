package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductSKUCatalogueResponse;
import com.products.application.dto.StockStatus;
import com.products.domain.entity.PriceType;
import com.products.domain.entity.ProductCatalogueSummaryView;
import org.springframework.stereotype.Component;

@Component
public class ProductSKUCatalogueMapper {
    public ProductSKUCatalogueResponse toResponse(ProductCatalogueSummaryView entity, Integer discountPercent) {
        return new ProductSKUCatalogueResponse(
                entity.getId(),
                entity.getSKU(),
                entity.getName(),
                StockStatus.fromStockCount(entity.getStockCount()),
                new ProductPriceCatalogueResponse(
                        entity.getOriginalPrice(),
                        entity.getCurrentPrice() == null ? entity.getOriginalPrice() : entity.getCurrentPrice(),
                        discountPercent == null ? 0 : discountPercent,
                        entity.getCurrentPriceTypeName() == null ? PriceType.Value.COMMON.getName() : entity.getCurrentPriceTypeName()
                )
        );
    }
}
