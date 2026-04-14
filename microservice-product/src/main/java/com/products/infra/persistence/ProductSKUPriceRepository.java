package com.products.infra.persistence;

import com.products.domain.entity.ProductSKUPrice;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductSKUPriceRepository extends JpaRepository<ProductSKUPrice, UUID> {
    @Query(
        """
        SELECT p 
        FROM ProductSKUPrice p
        WHERE p.isActive IS TRUE
        """
    )
    Page<ProductSKUPrice> findAllActive(Pageable pageable);

    @Query(
            """
            SELECT p 
            FROM ProductSKUPrice p
            WHERE p.productSKU.id = :productSKUId 
            AND p.isActive IS TRUE
            """
    )
    Page<ProductSKUPrice> findAllActiveByProductSKUId(@Param("productSKUId") UUID productSKUId, Pageable pageable);

    @Transactional
    @Query(
            """
            UPDATE ProductSKUPrice p
            SET p.isActive = FALSE
            WHERE p.productSKU.id = :productSKUId
            """
    )
    void setInactiveAllByProductSKUId(@Param("productSKUId") UUID productSKUId);
}
