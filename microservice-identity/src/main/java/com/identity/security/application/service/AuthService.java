package com.identity.security.application.service;

import com.identity.common.dto.UserCredentialsCreated;
import com.identity.common.event.UserCredentialsCreatedEvent;
import com.identity.security.application.dto.*;
import com.identity.security.application.exception.ForbiddenOperationException;
import com.identity.security.application.exception.UserAlreadyExistsException;
import com.identity.security.application.exception.UserNotFoundException;
import com.identity.security.domain.entity.RefreshToken;
import com.identity.security.domain.entity.UserCredentials;
import com.identity.security.domain.entity.UserRole;
import com.identity.security.infra.persistence.RefreshTokenRepository;
import com.identity.security.infra.persistence.UserCredentialsRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600;
    private final long REFRESH_TOKEN_EXPIRATION_SECONDS = 1_209_600; // TWO WEEKS
    private final String ISSUER = "AUTH_MICROSERVICE";

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${jwt.public-key}")
    private RSAPublicKey publicKey;

    public AuthService(UserCredentialsRepository userCredentialsRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, RefreshTokenRepository refreshTokenRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userCredentialsRepository.existsByEmail(request.email()))
            throw new UserAlreadyExistsException("Email is already registered");

        String encryptedPassword = passwordEncoder.encode(request.password());

        UserCredentials newUserCredentials = new UserCredentials();
        newUserCredentials.setEmail(request.email());
        newUserCredentials.setFirstName(request.firstName());
        newUserCredentials.setLastName(request.lastName());
        newUserCredentials.setRole(UserRole.USER);
        newUserCredentials.setIsActive(true);
        newUserCredentials.setEncryptedPassword(encryptedPassword);

        UserCredentials createdUserCredentials = userCredentialsRepository.save(newUserCredentials);

        applicationEventPublisher.publishEvent(
                new UserCredentialsCreatedEvent(
                        new UserCredentialsCreated(
                                createdUserCredentials.getUserId(),
                                createdUserCredentials.getEmail(),
                                createdUserCredentials.getFirstName(),
                                createdUserCredentials.getLastName(),
                                createdUserCredentials.getCreatedAt()
                        ),
                        this
                )
        );
    }


    public LoginResponse login(LoginRequest request){
        UserCredentials userCredentials = userCredentialsRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: "+request.email()));

        if (!passwordEncoder.matches(request.password(), userCredentials.getEncryptedPassword()))
            throw new ForbiddenOperationException("Wrong password");

        return generateTokens(userCredentials.getUserId());
    }

    private LoginResponse generateTokens(UUID userId){
        TokenResponse accessToken = generateAccessToken(userId);
        TokenResponse refreshToken = generateRefreshToken(userId);

        return new LoginResponse(accessToken, refreshToken);
    }

    private TokenResponse generateAccessToken(UUID userId){
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS);

        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(userId.toString())
                .claim("token_type", "access")
                .build();

        String jwtValue = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new TokenResponse(jwtValue, expiration);
    }

    private TokenResponse generateRefreshToken(UUID userId){
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(REFRESH_TOKEN_EXPIRATION_SECONDS);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(expiration);
        refreshToken.setIsRevoked(false);

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        var claims = JwtClaimsSet.builder()
                .id(savedToken.getId().toString())
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(userId.toString())
                .claim("token_type", "refresh")
                .build();

        String jwtValue = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new TokenResponse(jwtValue, expiration);
    }


    @Transactional
    public LoginResponse refresh(@Valid RefreshRequest request) {
        Jwt jwt = decodeToken(request.refreshToken());

        UUID tokenId = getTokenId(jwt);
        UUID userId = getUserId(jwt);
        String type = getClaimType(jwt);

        if (!"refresh".equals(type) || !userCredentialsRepository.existsById(userId))
            throw new BadJwtException("Invalid refresh token");

        revokeRefreshToken(tokenId);

        return generateTokens(userId);
    }

    private Jwt decodeToken(String token) {
        try {
            return jwtDecoder.decode(token);
        }
        catch (Exception e){
            throw new BadJwtException("Invalid token");
        }
    }

    private UUID getTokenId(Jwt jwt){
        try {
            return UUID.fromString(jwt.getId());
        } catch (Exception e){
            throw new BadJwtException("Invalid token");
        }
    }

    private UUID getUserId(Jwt jwt){
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (Exception e){
            throw new BadJwtException("Invalid token");
        }
    }

    private String getClaimType(Jwt jwt){
        try {
            return jwt.getClaimAsString("token_type");
        } catch (Exception e){
            throw new BadJwtException("Invalid token");
        }
    }

    private void revokeRefreshToken(UUID tokenId){
        RefreshToken refreshToken = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new BadJwtException("Invalid refresh token"));

        if (refreshToken.getIsRevoked())
            throw new BadJwtException("Refresh token is revoked");

        refreshToken.setIsRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    public Map<String, Object> getPublicJWKS(){
        RSAKey jwk = new RSAKey.Builder(publicKey)
//                .keyID()
                .build();
        return new JWKSet(jwk).toJSONObject();
    }
}
