package com.identity.security.infra.persistence;

import com.identity.security.domain.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {
    boolean existsByEmail(String email);

    Optional<UserCredentials> findByEmail(String email);
}
