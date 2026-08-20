package com.identity.security.application.dto;

public record LoginResponse (
        TokenResponse accessToken,
        TokenResponse refreshToken
) {
}
