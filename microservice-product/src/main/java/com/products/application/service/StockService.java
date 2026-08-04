package com.products.application.service;

import com.products.application.dto.admin.CreateStockMovementRequest;
import com.products.domain.entity.StockMovement;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.UUID;

public interface StockService {
    StockMovement createMovement(CreateStockMovementRequest request, UUID userId);

    Window<StockMovement> getAllMovements(ScrollPosition position, Limit limit);

    Window<StockMovement> getAllMovements(String SKU, ScrollPosition position, Limit limit);

    Window<StockMovement> getAllMovements(Integer typeId, ScrollPosition position, Limit limit);

    Window<StockMovement> getAllMovements(String SKU, Integer typeId, ScrollPosition position, Limit limit);
}
