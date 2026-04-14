package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@IdClass(ShoppingCartItemId.class)
public class ShoppingCartItem {
    @Id
    private UUID userId;

    @Id
    @JoinColumn(name = "product_sku_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductSKU productSKU;

    @NotNull @Positive
    private Integer units;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
