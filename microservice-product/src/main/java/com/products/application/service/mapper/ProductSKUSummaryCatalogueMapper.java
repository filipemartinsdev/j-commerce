package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductSKUSummaryCatalogueResponse;
import com.products.application.dto.StockStatus;
import com.products.domain.entity.ProductSKUSummaryCatalogue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductSKUSummaryCatalogueMapper {
    public ProductSKUSummaryCatalogueResponse toResponse(ProductSKUSummaryCatalogue entity, Integer discountPercent) {
        return new ProductSKUSummaryCatalogueResponse(
                entity.getId(),
                entity.getSKU(),
                entity.getName(),
                StockStatus.fromStockCount(entity.getStockCount()),
                new ProductPriceCatalogueResponse(
                        entity.getOriginalPrice(),
                        entity.getCurrentPrice() == null ? BigDecimal.ZERO : entity.getCurrentPrice(),
                        discountPercent,
                        entity.getCurrentPriceTypeName()
                )
        );
    }
}
