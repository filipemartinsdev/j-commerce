package com.products.infra.persistence;

import com.products.domain.entity.ProductSKU;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductSKURepository extends JpaRepository<ProductSKU, UUID> {

    @Query(
            """
            SELECT sku
            FROM ProductSKU sku
            WHERE sku.isActive IS TRUE 
            AND sku.product.id = :productId
            """
    )
    Page<ProductSKU> findAllActiveByProductId(@Param("productId") UUID productId, Pageable pageable);

    @Query(
            """
            SELECT sku
            FROM ProductSKU sku
            WHERE sku.isActive IS TRUE 
            AND sku.id = :id
            """
    )
    Optional<ProductSKU> findActiveById(@Param("id") UUID id);

    @Query(
            """
            SELECT sku
            FROM ProductSKU sku
            WHERE sku.isActive IS TRUE 
            """
    )
    Page<ProductSKU> findAllActive(Pageable pageable);
}
