package com.products.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "product_sku_summary_catalogue")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductSKUSummaryCatalogue {
    @Id
    private UUID id;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "sku")
    private String SKU;

    private String name;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "stock_count")
    private Integer stockCount;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "current_price_type_id")
    private Integer currentPriceTypeId;

    @Column(name = "current_price_type_name")
    private String currentPriceTypeName;
}
