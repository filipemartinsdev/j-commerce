package com.products.infra.persistence;

import com.products.domain.entity.ShoppingCartItemSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShoppingCartItemSummaryViewRepository extends JpaRepository<ShoppingCartItemSummaryView, UUID> {
    @Query(
            """
            SELECT s
            FROM ShoppingCartItemSummaryView s
            WHERE s.userId = :userId
            """
    )
    Page<ShoppingCartItemSummaryView> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    List<ShoppingCartItemSummaryView> findAllByUserId(@Param("userId") UUID userId);
}
