package com.products.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class ShoppingCart implements Serializable {
    private List<Item> items;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class Item implements Serializable {
        private String SKU;
        private String name;
        private Integer units;
        private BigDecimal price;
    }
}
