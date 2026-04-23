package com.products.infra.persistence;

import com.products.domain.entity.ShoppingCartItemProductSKUSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShoppingCartItemProductSKUSummaryRepository extends JpaRepository<ShoppingCartItemProductSKUSummary, UUID> {
    @Query(
            """
            SELECT s
            FROM ShoppingCartItemProductSKUSummary s
            WHERE s.userId = :userId
            """
    )
    Page<ShoppingCartItemProductSKUSummary> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    List<ShoppingCartItemProductSKUSummary> findAllByUserId(@Param("userId") UUID userId);
}
