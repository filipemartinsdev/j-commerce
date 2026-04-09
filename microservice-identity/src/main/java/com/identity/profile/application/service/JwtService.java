package com.identity.profile.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Slf4j
public class JwtService {
    public UUID getUserId(Jwt jwt) {
        return UUID.fromString(
                jwt.getSubject()
        );
    }
}
