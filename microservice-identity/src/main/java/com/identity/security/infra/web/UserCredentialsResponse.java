package com.identity.security.infra.web;

import java.time.Instant;
import java.util.UUID;

public record UserCredentialsResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant createdAt
) {
}
