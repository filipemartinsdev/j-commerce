package com.orders.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record CreateDeliveryAddressRequest(
        @NotBlank @Length(min = 8, max = 8) String zipCode,
        @NotBlank @Length(max = 255) String street,
        @NotBlank @Length(max = 20) String number,
        Optional<@Length(max = 255) String> complement,
        @NotBlank @Length(max = 50) String neighborhood,
        @NotBlank @Length(max = 100) String city,
        @NotBlank @Length(max = 2) String state,
        Optional<Double> latitude,
        Optional<Double> longitude
) {
}
