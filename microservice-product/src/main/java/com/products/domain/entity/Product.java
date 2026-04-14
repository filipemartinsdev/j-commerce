package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "product")
@Data @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;
    private String description;

    @NotNull
    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductCategory category;

    @NotNull
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @SQLRestriction("is_active = true")
    private List<ProductSKU> SKUs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @NotNull
    @Column(name = "is_active")
    private boolean isActive = true;
}
