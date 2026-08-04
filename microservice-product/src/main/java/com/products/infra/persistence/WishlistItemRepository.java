package com.products.infra.persistence;

import com.products.domain.entity.WishlistItem;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends MongoRepository<WishlistItem, String> {
    @Query(value = "{ userId: ?0 }")
    Window<WishlistItem> findAllByUserId(UUID userId, ScrollPosition position, Limit limit);

    void deleteAllByUserId(UUID userId);

    Optional<WishlistItem> findByUserIdAndProductId(UUID userId, String productId);

    boolean existsByUserIdAndProductId(UUID userId, String productId);
}
