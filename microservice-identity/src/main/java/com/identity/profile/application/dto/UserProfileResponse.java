package com.identity.profile.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}
