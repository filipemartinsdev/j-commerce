package com.identity.security.application.dto;

import org.antlr.v4.runtime.Token;

public record LoginResponse (
        TokenResponse accessToken,
        TokenResponse refreshToken
) {
}
