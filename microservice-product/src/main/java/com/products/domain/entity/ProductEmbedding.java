package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity @Table(name = "product_embedding")
@Data @AllArgsConstructor @NoArgsConstructor
public class ProductEmbedding {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id")
    @NotNull
    private String productId;

    @Column(name = "category_id")
    @NotNull
    private Long categoryId;

    @Column(name = "embedding")
    @Array(length = 1536)
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;
}
