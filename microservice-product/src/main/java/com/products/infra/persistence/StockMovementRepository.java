package com.products.infra.persistence;

import com.products.domain.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    Page<StockMovement> findAllByProductSKU_id(UUID productStock_productSKU_id, Pageable pageable);
}
