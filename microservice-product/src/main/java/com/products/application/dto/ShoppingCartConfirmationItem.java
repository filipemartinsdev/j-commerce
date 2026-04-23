package com.products.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class ShoppingCartConfirmationItem implements Serializable {
    private UUID productSKUId;
    private String name;
    private Integer units;
    private BigDecimal unitPrice;
}
