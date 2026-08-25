package com.products.application.service;

import com.products.application.dto.catalogue.ShoppingCartResponse;

import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartResponse get(UUID userId);

    void add(UUID userId, String SKU, int units);

    void remove(UUID userId, String SKU);

    void clear(UUID userId);

    void confirm(UUID userId, UUID deliveryAddressId, String BearerJWT);
}
