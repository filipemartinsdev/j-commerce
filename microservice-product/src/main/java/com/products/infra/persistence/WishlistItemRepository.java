package com.products.infra.persistence;

import com.products.domain.entity.WishlistItem;
import com.products.domain.entity.WishlistItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, WishlistItemId> {
}
