package com.orders.application.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor @AllArgsConstructor
public class ShoppingCartConfirmationMessage implements Serializable {
    private UUID userId;
    private List<ShoppingCartConfirmationItem> items;
    private UUID deliveryAddressId;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ShoppingCartConfirmationItem implements Serializable {
        private UUID productSKUId;
        private String name;
        private Integer units;
        private BigDecimal unitPrice;
    }
}
