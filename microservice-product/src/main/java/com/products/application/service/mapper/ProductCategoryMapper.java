package com.products.application.service.mapper;

import com.products.application.dto.ProductCategoryResponse;
import com.products.domain.entity.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper {
    public ProductCategoryResponse toResponse(ProductCategory productCategory) {
        return new ProductCategoryResponse(
                productCategory.getId(),
                productCategory.getName()
        );
    }
}
