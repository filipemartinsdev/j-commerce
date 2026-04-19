package com.products.infra.persistence;

import com.products.domain.entity.WishlistItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    @Transactional
    @Modifying
    @Query(
            """
            UPDATE WishlistItem w
            SET w.isActive = FALSE
            WHERE w.isActive IS TRUE
            AND w.userId = :userId
            """
    )
    void markAllAsInactiveByUserId(@Param("userId") UUID userId);

    @Query(
            """
            SELECT COUNT(w) > 0
            FROM WishlistItem w
            WHERE w.isActive IS TRUE
            AND w.userId = :userId
            AND w.productSKU.id = :productSKUId
            """
    )
    boolean existsByProductSKUIdAndUserId(@Param("productSKUId") UUID productSKUId, @Param("userId") UUID userId);

    @Query(
            """
            SELECT w
            FROM WishlistItem w
            WHERE w.id = :id
            AND w.userId = :userId
            AND w.isActive IS TRUE
            """
    )
    Optional<WishlistItem> findActiveByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
