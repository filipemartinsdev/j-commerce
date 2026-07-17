package com.products.infra.persistence;

import com.products.domain.entity.ProductSKUPrice;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductSKUPriceRepository extends JpaRepository<ProductSKUPrice, UUID> {
    @Query(
        """
        SELECT p
        FROM ProductSKUPrice p
        WHERE p.isActive IS TRUE
            AND (p.endAt > CURRENT_TIMESTAMP OR p.endAt IS NULL)
            AND p.productSKU.id = :productSKUId
        ORDER BY p.priceType.id DESC
        """
    )
    Optional<ProductSKUPrice> findFirstCurrentPrice(@Param("productSKUId") UUID productSKUId);

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
    @Modifying
    @Query(
            """
            UPDATE ProductSKUPrice p
            SET p.isActive = FALSE
            WHERE p.productSKU.id = :productSKUId
            """
    )
    void setInactiveAllByProductSKUId(@Param("productSKUId") UUID productSKUId);

    @Query(
            """
            SELECT p
            FROM ProductSKUPrice p
            WHERE p.isActive IS TRUE
            AND p.priceType.id = 1
            AND p.endAt IS NULL
            AND p.productSKU.id = :productSKUId
            """
    )
    List<ProductSKUPrice> findAllActiveBasePriceByProductSKUId(@Param("productSKUId") UUID productSKUId);
}
