package com.products.infra.persistence;

import com.products.domain.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementTypeRepository extends JpaRepository<StockMovementType, Integer> {
}
