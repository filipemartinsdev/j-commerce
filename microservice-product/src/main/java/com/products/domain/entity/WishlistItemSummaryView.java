package com.products.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "wishlist_item_summary_view")
@Data @NoArgsConstructor @AllArgsConstructor
public class WishlistItemSummaryView {
    @Id private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "product_sku_id")
    private UUID productSKUId;

    @Column(name = "product_sku_name")
    private String productSKUName;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "price_type_id")
    private Integer priceTypeId;

    @Column(name = "price_type_name")
    private String priceTypeName;
}
