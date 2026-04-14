package com.products.application.dto.admin;

import java.time.Instant;
import java.util.UUID;

public record ProductAdminResponse (
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
