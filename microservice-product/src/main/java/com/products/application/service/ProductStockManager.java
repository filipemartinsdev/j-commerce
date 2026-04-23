package com.products.application.service;

import com.products.application.exception.ProductOutOfStockException;
import com.products.application.exception.ProductStockNotFoundException;
import com.products.infra.persistence.ProductStockRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductStockManager {
    private final ProductStockRepository productStockRepository;

    public ProductStockManager(ProductStockRepository productStockRepository) {
        this.productStockRepository = productStockRepository;
    }

    public void reduceProductStock(UUID productSKUId, int units){
        var stock = productStockRepository.findByProductSKU_id(productSKUId)
            .orElseThrow(() -> new ProductStockNotFoundException("Product stock not found with productSKUId: "+productSKUId));

        if (!stock.getIsActive())
            throw new ProductStockNotFoundException("Product stock not found with productSKUId: "+productSKUId);

        if (stock.getUnits() < units)
            throw new ProductOutOfStockException("Product out of stock with productSKUId: "+productSKUId);

        stock.setUnits(stock.getUnits() - units);
        productStockRepository.save(stock);
    }
}
