package com.orders.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.Optional;

public record CreateDeliveryAddressRequest(
        @NotNull Boolean byCoordinates,
        @NotNull Boolean haveNumber,
        Optional<String> zipCode,
        Optional<String> street,
        Optional<String> number,
        Optional<String> complement,
        Optional<String> neighborhood,
        Optional<String> city,
        Optional<String> state,
        Optional<Double> latitude,
        Optional<Double> longitude
) {
}
