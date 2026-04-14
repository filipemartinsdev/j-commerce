package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "product_sku_price")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductSKUPrice {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @JoinColumn(name = "product_sku_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductSKU productSKU;

    @NotNull @PositiveOrZero
    private BigDecimal price;

    @NotNull
    @JoinColumn(name = "price_type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PriceType priceType;

    @NotNull
    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;
}
