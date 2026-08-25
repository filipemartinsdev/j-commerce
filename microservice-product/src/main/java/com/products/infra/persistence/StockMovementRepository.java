package com.products.infra.persistence;

import com.products.domain.entity.StockMovement;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockMovementRepository extends MongoRepository<StockMovement, String> {
    Window<StockMovement> findAllByOrderById(ScrollPosition position, Limit limit);

    Window<StockMovement> findAllBySKUOrderById(String SKU, ScrollPosition position, Limit limit);

    Window<StockMovement> findAllByTypeIdOrderById(Integer typeId, ScrollPosition position, Limit limit);

    Window<StockMovement> findAllBySKUAndTypeIdOrderById(String SKU, Integer typeId, ScrollPosition position, Limit limit);
}

