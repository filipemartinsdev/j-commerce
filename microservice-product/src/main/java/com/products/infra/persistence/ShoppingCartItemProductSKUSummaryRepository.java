package com.products.infra.persistence;

import com.products.domain.entity.ShoppingCartItemProductSKUResume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShoppingCartItemProductSKUResponseRepository extends JpaRepository<ShoppingCartItemProductSKUResume, UUID> {
    @Query(
            """
            SELECT s
            FROM ShoppingCartItemProductSKUResume s
            WHERE s.userId = :userId
            """
    )
    Page<ShoppingCartItemProductSKUResume> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    List<ShoppingCartItemProductSKUResume> findAllByUserId(@Param("userId") UUID userId);
}
