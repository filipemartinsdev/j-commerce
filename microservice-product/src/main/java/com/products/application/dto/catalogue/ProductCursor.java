package com.products.application.dto.catalogue;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductCursor (
        UUID lastId,
        float lastDistance
) {
}
