package com.orders.application.dto;

import java.time.Instant;
import java.util.UUID;

public record DeliveryAddressResponse(
        UUID id,
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        Double latitude,
        Double longitude,
        Instant createdAt
) {
}
