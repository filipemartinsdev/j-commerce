package com.products.infra.persistence;

import com.products.domain.entity.ShoppingCartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, UUID> {
    @Transactional
    @Modifying
    @Query(
            """
            UPDATE ShoppingCartItem s
            SET s.isActive = FALSE
            WHERE s.isActive IS TRUE
            AND s.userId = :userId
            """
    )
    void markAllAsInactiveByUserId(@Param("userId") UUID userId);

    @Query(
            """
            SELECT s 
            FROM ShoppingCartItem s
            WHERE s.isActive IS TRUE 
            AND s.id = :id
            AND s.userId = :userId
            """
    )
    Optional<ShoppingCartItem> findActiveByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(
            """
            SELECT COUNT(s) > 0
            FROM ShoppingCartItem s
            WHERE s.isActive IS TRUE
            AND s.productSKU.id = :productSKUId
            AND s.userId = :userId
            """
    )
    boolean existsByProductSKUIdAndUserId(@Param("productSKUId") UUID productSKUId, @Param("userId") UUID userId);
}
