package com.jcommerce.web.client.dto;

import java.time.Instant;

public record LoginResponse(
        Data data,
        String status
) {
    public static record Data (
            Token accessToken,
            Token refreshToken
    ){}

    public static record Token (
            String token,
            Instant expiration
    ){}
}
