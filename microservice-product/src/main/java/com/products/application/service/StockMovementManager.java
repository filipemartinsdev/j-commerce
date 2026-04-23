package com.products.application.service;

import com.products.domain.entity.StockMovement;
import com.products.domain.entity.StockMovementType;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.StockMovementRepository;
import com.products.infra.persistence.StockMovementTypeRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StockMovementManager {
    private final StockMovementRepository stockMovementRepository;
    private final ProductSKURepository productSKURepository;
    private final StockMovementTypeRepository stockMovementTypeRepository;

    public StockMovementManager(StockMovementRepository stockMovementRepository, ProductSKURepository productSKURepository, StockMovementTypeRepository stockMovementTypeRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productSKURepository = productSKURepository;
        this.stockMovementTypeRepository = stockMovementTypeRepository;
    }

    public void registerSale(UUID productSKUId, int units, UUID userId){
        StockMovement movement = new StockMovement();
        movement.setProductSKU(productSKURepository.getReferenceById(productSKUId));
        movement.setUnits(units);
        movement.setCreatedBy(userId);
        movement.setType(stockMovementTypeRepository.getReferenceById(
                StockMovementType.Value.SALE.getId()
        ));

        stockMovementRepository.save(movement);
    }
}
