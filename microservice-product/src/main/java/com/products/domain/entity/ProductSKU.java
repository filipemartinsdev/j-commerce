package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "product_sku")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductSKU {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @JoinColumn(name = "product_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @NotBlank @Length(max = 50)
    @Column(name = "sku")
    private String SKU;

    @NotBlank @Length(max = 255)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;
}
