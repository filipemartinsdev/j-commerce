package com.products.domain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class ShoppingCartItemId implements Serializable {
    private UUID userId;
    private ProductSKU productSKU;
}
