package com.identity.common.dto;

import java.time.Instant;
import java.util.UUID;

public record UserCredentialsCreated(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}
