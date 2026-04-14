package com.products.application.service.mapper;

import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.domain.entity.StockMovementType;
import org.springframework.stereotype.Component;

@Component
public class StockMovementTypeMapper {
    public StockMovementTypeResponse toResponse(StockMovementType entity){
        return new StockMovementTypeResponse(
                entity.getId(),
                entity.getName()
        );
    }
}
