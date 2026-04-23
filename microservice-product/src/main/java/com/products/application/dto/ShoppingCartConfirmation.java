package com.products.application.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class ShoppingCartConfirmation implements Serializable {
    private UUID userId;
    private List<ShoppingCartConfirmationItem> items;
    private UUID deliveryAddressId;
}
