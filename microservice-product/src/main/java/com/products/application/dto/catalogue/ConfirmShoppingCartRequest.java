package com.products.application.dto.catalogue;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmShoppingCartRequest(
        @NotNull UUID deliveryAddressId
) {
}
