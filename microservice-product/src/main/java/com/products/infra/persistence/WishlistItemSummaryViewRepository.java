package com.products.infra.persistence;

import com.products.domain.entity.WishlistItemSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistItemSummaryViewRepository extends JpaRepository<WishlistItemSummaryView, UUID> {
    Page<WishlistItemSummaryView> findAllByUserId(UUID userId, Pageable pageable);
}
