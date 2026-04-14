package com.products.infra.persistence;

import com.products.domain.entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {
    Optional<ProductStock> findByProductSKU_id(UUID productSKU_id);

    @Query(
        """
        SELECT ps
        FROM ProductStock ps
        WHERE ps.isActive IS TRUE
        AND ps.productSKU.product.id = :productId
        """
    )
    Page<ProductStock> findAllActiveByProductId(@Param("productId") UUID productId, Pageable pageable);

    @Query(
        """
        SELECT ps
        FROM ProductStock ps
        WHERE ps.isActive IS TRUE
        """
    )
    Page<ProductStock> findAllActive(Pageable pageable);

    @Query(
            """
            SELECT ps
            FROM ProductStock ps
            WHERE ps.isActive IS TRUE
            AND ps.id = :id
            """
    )
    Optional<ProductStock> findActiveById(@Param("id") UUID id);
}
