package com.identity.security.application.dto;

import java.time.Instant;

public record TokenResponse (
        String token,
        Instant expiration
) {

}
