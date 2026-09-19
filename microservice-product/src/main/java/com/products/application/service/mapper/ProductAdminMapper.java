package com.products.application.service.mapper;

import com.products.application.dto.admin.AdminProductResponse;
import com.products.application.service.BucketService;
import com.products.model.entity.Product;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

@Component
public class ProductAdminMapper {
    public AdminProductResponse toResponse(Product entity){
        return new AdminProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new AdminProductResponse.CategorySummary(
                        entity.getCategory().getId(),
                        entity.getCategory().getName()
                ),
                entity.getSKUs().stream()
                        .map(this::toSKUResponse)
                        .toList(),
                null,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }

    private AdminProductResponse.SKU toSKUResponse(Product.ProductSKU productSKU) {
        return new AdminProductResponse.SKU(
                productSKU.getSKU(),
                productSKU.getName(),
                productSKU.getStock(),
                new AdminProductResponse.Price(
                        productSKU.getBasePrice().getLabel(),
                        productSKU.getBasePrice().getValue()
                ),
                new AdminProductResponse.Price(
                        productSKU.getCurrentPrice().getLabel(),
                        productSKU.getCurrentPrice().getValue()
                ),
                productSKU.getAttributes().stream()
                        .map(attribute -> new AdminProductResponse.Attribute(
                                attribute.getName(),
                                attribute.getValue()
                        ))
                        .toList(),
                productSKU.getCreatedAt(),
                productSKU.getCreatedBy(),
                productSKU.getUpdatedAt(),
                productSKU.getUpdatedBy()
        );
    }
}
