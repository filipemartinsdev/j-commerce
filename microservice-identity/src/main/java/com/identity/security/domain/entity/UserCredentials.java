package com.identity.security.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "user_credentials")
@Data @NoArgsConstructor @AllArgsConstructor
public class UserCredentials {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Email
    @Column(name = "email", unique = true)
    private String email;

    @NotNull
    @Column(name = "encrypted_password")
    private String encryptedPassword;

    @NotBlank
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Column(name = "last_name")
    private String lastName;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;

    @NotNull
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();
}
