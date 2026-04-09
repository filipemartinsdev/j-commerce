package com.identity.security.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "refresh_token")
@Data @NoArgsConstructor @AllArgsConstructor
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @Column(name = "expires_at")
    private Instant expiresAt;

    @NotNull
    @Column(name = "is_revoked")
    private Boolean isRevoked;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
