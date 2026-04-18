package com.products.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "wishlist_item")
@Data @NoArgsConstructor @AllArgsConstructor
@IdClass(WishlistItemId.class)
public class WishlistItem {
    @Id
    private UUID userId;

    @Id
    @JoinColumn(name = "product_sku_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductSKU productSKU;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
