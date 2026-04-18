package com.products.infra.persistence;

import com.products.domain.entity.WishlistItemProductSKUResume;
import com.products.domain.entity.WishlistItemProductSKUResumeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WishlistItemProductSKUResumeRepository extends JpaRepository<WishlistItemProductSKUResume, WishlistItemProductSKUResumeId> {
    Page<WishlistItemProductSKUResume> findAllByUserId(UUID userId, Pageable pageable);
}
