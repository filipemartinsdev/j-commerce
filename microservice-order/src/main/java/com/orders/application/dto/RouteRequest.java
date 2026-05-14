package com.orders.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RouteRequest (
        @NotBlank String profile,
        @NotNull Double[][] points
) {
}
