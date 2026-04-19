package com.products.infra.persistence;

import com.products.domain.entity.WishlistItemProductSKUResume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistItemProductSKUResumeRepository extends JpaRepository<WishlistItemProductSKUResume, UUID> {
    Page<WishlistItemProductSKUResume> findAllByUserId(UUID userId, Pageable pageable);
}
