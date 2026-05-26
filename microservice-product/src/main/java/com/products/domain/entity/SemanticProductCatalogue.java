package com.products.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "product_resume")
@Immutable
@Data @NoArgsConstructor @AllArgsConstructor
public class SemanticProductCatalogue {
    @Id @Column(name = "id")
    private UUID productId;

    private String name;

    private String description;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "original_price_value")
    private BigDecimal originalPriceValue;

    @Column(name = "current_price_type_id")
    private Integer currentPriceTypeId;

    @Column(name = "current_price_type_name")
    private String currentPriceTypeName;

    @Column(name = "current_price_value")
    private BigDecimal currentPriceValue;

    @Column(name = "stock_count")
    private Integer stockCount;

    @Column(name = "embedding")
    @Array(length = 1536)
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;
}
