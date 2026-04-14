package com.products.application.service.mapper;

import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.domain.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {
    public StockMovementResponse toResponse(StockMovement entity) {
        return new StockMovementResponse(
                entity.getId(),
                entity.getProductSKU().getId(),
                entity.getProductSKU().getName(),
                entity.getUnits(),
                new StockMovementTypeResponse(
                        entity.getType().getId(),
                        entity.getType().getName()
                ),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }
}
