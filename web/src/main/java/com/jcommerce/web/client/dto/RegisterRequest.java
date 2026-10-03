package com.jcommerce.web.client.dto;

public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName
) {
}
