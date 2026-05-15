package com.orders.application.dto;

import java.time.Instant;
import java.util.UUID;

public record StorageAddressResponse(
        UUID id,
        String zipCode,
        String street,
        String complement,
        String neighborhood,
        String city,
        String state,
        Double latitude,
        Double longitude,
        Instant createdAt
) {
}
