package com.products.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "wishlist_item_product_sku_resume")
@Data @NoArgsConstructor @AllArgsConstructor
@IdClass(WishlistItemProductSKUResumeId.class)
public class WishlistItemProductSKUResume {
    @Column(name = "user_id")
    @Id private UUID userId;

    @Column(name = "product_sku_id")
    @Id private UUID productSKUId;

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
