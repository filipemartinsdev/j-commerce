package com.products.application.service.mapper;

import com.products.application.dto.admin.ProductStockResponse;
import com.products.domain.entity.ProductStock;
import org.springframework.stereotype.Component;

@Component
public class ProductStockMapper {
    public ProductStockResponse toResponse(ProductStock entity) {
        return new ProductStockResponse(
            entity.getId(),
            entity.getProductSKU().getProduct().getId(),
            entity.getProductSKU().getId(),
            entity.getProductSKU().getName(),
            entity.getProductSKU().getSKU(),
            entity.getUnits(),
            entity.getUpdatedAt()
        );
    }
}
