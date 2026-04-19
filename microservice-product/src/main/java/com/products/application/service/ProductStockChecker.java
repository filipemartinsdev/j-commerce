package com.products.application.service;

import com.products.application.exception.ProductStockNotFoundException;
import com.products.domain.entity.ProductStock;
import com.products.infra.persistence.ProductStockRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductStockChecker {
    private final ProductStockRepository productStockRepository;

    public ProductStockChecker(ProductStockRepository productStockRepository) {
        this.productStockRepository = productStockRepository;
    }

    public boolean isTheProductWithStockEnough(UUID productSKUId, Integer units) {
        ProductStock stock = productStockRepository.findByProductSKU_id(productSKUId)
                .orElseThrow(() -> new ProductStockNotFoundException("Product stock not found with productSKUId: "+productSKUId));
        return stock.getUnits() >= units;
    }
}
