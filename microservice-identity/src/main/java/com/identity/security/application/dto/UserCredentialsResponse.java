package com.identity.security.application.dto;

import com.identity.security.domain.entity.Role;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserCredentialsResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        List<Role.Value> roles,
        Instant createdAt
) {
}
