package com.products.application.service.mapper;

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
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
