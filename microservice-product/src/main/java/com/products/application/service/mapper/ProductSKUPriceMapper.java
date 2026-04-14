package com.products.application.service.mapper;

import com.products.application.dto.PriceTypeResponse;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.domain.entity.PriceType;
import com.products.domain.entity.ProductSKUPrice;
import org.springframework.stereotype.Component;

@Component
public class ProductSKUPriceMapper {
    public ProductSKUPriceResponse toResponse(ProductSKUPrice entity) {
        return new ProductSKUPriceResponse(
                entity.getId(),
                entity.getProductSKU().getId(),
                entity.getProductSKU().getName(),
                entity.getPrice(),
                new PriceTypeResponse(
                        entity.getPriceType().getId(),
                        entity.getPriceType().getName()
                ),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getCreatedAt()
        );
    }
}
