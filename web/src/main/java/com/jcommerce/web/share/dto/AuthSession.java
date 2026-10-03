package com.jcommerce.web.share.dto;

public record AuthSession(
        String accessToken,
        String refreshToken
) {
}
