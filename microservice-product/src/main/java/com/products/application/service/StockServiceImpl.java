package com.products.application.service;

import com.products.application.dto.admin.CreateStockMovementRequest;
import com.products.application.exception.InvalidStockMovementReasonException;
import com.products.application.exception.InvalidStockMovementTypeException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.domain.entity.StockMovement;
import com.products.infra.persistence.ProductRepository;
import com.products.infra.persistence.StockMovementRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class StockServiceImpl implements StockService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public StockServiceImpl(StockMovementRepository stockMovementRepository, ProductRepository productRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }

    @Override @Transactional
    public StockMovement createMovement(CreateStockMovementRequest request, UUID userId) {
        var type = getStockMovementTypeById(request.typeId());
        var reason = getStockMovementReasonById(request.reasonId());

        StockMovement movement = assembleStockMovement(request, type, reason, userId);

        if (type == StockMovement.Type.INBOUND)
            increaseStock(request.SKU(), request.units(), userId);
        else if (type == StockMovement.Type.OUTBOUND)
            decreaseStock(request.SKU(), request.units(), userId);

        return stockMovementRepository.save(movement);
    }

    private void increaseStock(String SKU, Integer units, UUID userId) {
        var product = productRepository.findBySKUWithLock(SKU)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: "+SKU));

        var productSKU = product.findSKU(SKU)
                .orElseThrow(() -> new ProductSKUNotFoundException("SKU not found: "+SKU));

        productSKU.setStock(productSKU.getStock() + units);
        product.setUpdatedAt(Instant.now());
        productSKU.setUpdatedBy(userId);

        productRepository.save(product);
    }

    private void decreaseStock(String SKU, Integer units, UUID userId) {
        var product = productRepository.findBySKUWithLock(SKU)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: "+SKU));

        var productSKU = product.findSKU(SKU)
                .orElseThrow(() -> new ProductSKUNotFoundException("SKU not found: "+SKU));

        productSKU.setStock(productSKU.getStock() - units);
        product.setUpdatedAt(Instant.now());
        productSKU.setUpdatedBy(userId);

        productRepository.save(product);
    }

    private StockMovement assembleStockMovement(
            CreateStockMovementRequest request,
            StockMovement.Type type,
            StockMovement.Reason reason,
            UUID userId
    ) {
        var movement = new StockMovement();

        movement.setType(new StockMovement.MovementType(type.id, type.name()));
        movement.setReason(new StockMovement.MovementReason(reason.id, reason.name()));

        movement.setSKU(request.SKU());
        movement.setUnits(request.units());
        movement.setCreatedBy(userId);

        if (request.description().isPresent())
            movement.setDescription(request.description().get());

        return movement;
    }

    private StockMovement.Type getStockMovementTypeById(Integer id){
        return StockMovement.Type.getById(id)
                .orElseThrow(() -> new InvalidStockMovementTypeException("Invalid stock movement type by ID: "+id));
    }

    private StockMovement.Reason getStockMovementReasonById(Integer id){
        return StockMovement.Reason.getById(id)
                .orElseThrow(() -> new InvalidStockMovementReasonException("Invalid stock movement reason by ID: "+id));
    }


    @Override
    public Window<StockMovement> getAllMovements(ScrollPosition position, Limit limit) {
        return stockMovementRepository.findAllByOrderById(position, limit);
    }

    @Override
    public Window<StockMovement> getAllMovements(String SKU, ScrollPosition position, Limit limit) {
        return stockMovementRepository.findAllBySKUOrderById(SKU, position, limit);
    }

    @Override
    public Window<StockMovement> getAllMovements(Integer typeId, ScrollPosition position, Limit limit) {
        return stockMovementRepository.findAllByTypeIdOrderById(typeId, position, limit);
    }

    @Override
    public Window<StockMovement> getAllMovements(String SKU, Integer typeId, ScrollPosition position, Limit limit) {
        return stockMovementRepository.findAllBySKUAndTypeIdOrderById(SKU, typeId, position, limit);
    }
}
