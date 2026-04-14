package com.products.application.service.mapper;

import com.products.application.dto.admin.ProductSKUAdminResponse;
import com.products.domain.entity.ProductSKU;
import org.springframework.stereotype.Component;

@Component
public class ProductSKUAdminMapper {
    public ProductSKUAdminResponse toResponse(ProductSKU entity) {
        return new ProductSKUAdminResponse(
                entity.getId(),
                entity.getProduct().getId(),
                entity.getSKU(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIsActive()
        );
    }
}
