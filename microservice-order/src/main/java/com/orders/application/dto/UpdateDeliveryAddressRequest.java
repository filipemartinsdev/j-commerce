package com.orders.application.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.Optional;

public record UpdateDeliveryAddressRequest(
        Optional<@NotBlank @Length(min = 8, max = 8) String> zipCode,
        Optional<@NotBlank @Length(max = 255) String> street,
        Optional<@NotBlank @Length(max = 20) String> number,
        Optional<@NotBlank @Length(max = 255) String> complement,
        Optional<@NotBlank @Length(max = 50) String> neighborhood,
        Optional<@NotBlank @Length(max = 100) String> city,
        Optional<@NotBlank @Length(max = 2) String> state,
        Optional<Double> latitude,
        Optional<Double> longitude
) {
}
