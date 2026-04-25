package com.identity.security.application.dto;

import com.identity.security.domain.entity.Role;
import com.identity.security.domain.entity.UserRole;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;


public record UpdateUserRole(
        @NotNull List<Role.Value> roles
) {
}
