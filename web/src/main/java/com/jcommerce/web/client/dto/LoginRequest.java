package com.jcommerce.web.client.dto;

public record LoginRequest(
        String email,
        String password
) {
}
