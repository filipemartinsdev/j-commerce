package com.products.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class WishlistItemId implements Serializable {
    private UUID userId;
    private ProductSKU productSKU;
}
