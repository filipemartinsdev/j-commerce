package com.identity.security.application.service;

import com.identity.security.domain.entity.Role;
import com.identity.security.domain.entity.UserCredentials;
import com.identity.security.application.dto.UserCredentialsResponse;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialsMapper {
    public UserCredentialsResponse toResponse(UserCredentials entity) {
        return new UserCredentialsResponse(
                entity.getUserId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getRoles().stream()
                        .map(role -> Role.Value.fromId(role.getId()))
                        .toList(),
                entity.getCreatedAt()
        );
    }
}
