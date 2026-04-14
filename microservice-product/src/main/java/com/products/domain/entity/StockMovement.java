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

@Entity @Table(name = "stock_movement")
@Data @NoArgsConstructor @AllArgsConstructor
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @JoinColumn(name = "product_sku_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductSKU productSKU;

    @NotNull @Positive
    private Integer units;

    @NotNull
    @JoinColumn(name = "type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private StockMovementType type;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "created_by")
    private UUID createdBy;
}
