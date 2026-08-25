package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.domain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductCatalogueMapper {
    public ProductCatalogueResponse toResponse(Product entity){
        return new ProductCatalogueResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                new ProductCatalogueResponse.Category(
                        entity.getCategory().getId(),
                        entity.getCategory().getName()
                ),
                entity.getSKUs().stream()
                        .map(skuEntity ->
                                new ProductCatalogueResponse.ProductSKU(
                                        skuEntity.getSKU(),
                                        skuEntity.getName(),
                                        skuEntity.getStock(),
                                        new ProductCatalogueResponse.ProductSKU.Price(
                                                skuEntity.getCurrentPrice().getLabel(),
                                                skuEntity.getCurrentPrice().getValue()
                                        ),
                                        new ProductCatalogueResponse.ProductSKU.Price(
                                                skuEntity.getBasePrice().getLabel(),
                                                skuEntity.getBasePrice().getValue()
                                        ),
                                        skuEntity.getAttributes().stream()
                                                .map(att -> new ProductCatalogueResponse.ProductSKU.Attribute(
                                                        att.getName(), att.getValue()
                                                ))
                                                .toList()
                                )
                        )
                        .toList()
        );
    }
}
