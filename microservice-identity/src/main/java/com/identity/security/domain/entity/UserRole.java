package com.identity.security.domain.entity;

import lombok.Getter;

public enum UserRole {
    USER("user"), ADMIN("admin");

    @Getter
    private final String name;

    UserRole(String name) {
        this.name = name;
    }
}
