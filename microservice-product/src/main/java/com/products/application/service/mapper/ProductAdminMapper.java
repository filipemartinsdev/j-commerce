package com.products.application.service.mapper;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.admin.ProductAdminResponse;
import com.products.domain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductAdminMapper {
    public ProductAdminResponse toResponse(Product entity){
        return new ProductAdminResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new ProductCategoryResponse(
                        entity.getCategory().getId(),
                        entity.getCategory().getName()
                ),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
