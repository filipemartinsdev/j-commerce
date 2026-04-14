package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "product_stock")
public class ProductStock {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "product_sku_id")
    @OneToOne(fetch = FetchType.LAZY)
    private ProductSKU productSKU;

    @NotNull
    @PositiveOrZero
    private Integer units = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "created_by")
    private UUID createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;
}
