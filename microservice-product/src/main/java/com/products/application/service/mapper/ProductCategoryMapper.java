package com.products.application.service.mapper;

import com.products.application.dto.catalogue.CategoryCatalogueResponse;
import com.products.domain.entity.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper {
    public CategoryCatalogueResponse toResponse(ProductCategory entity) {
        return new CategoryCatalogueResponse(
                entity.getId(),
                entity.getName()
        );
    }
}
