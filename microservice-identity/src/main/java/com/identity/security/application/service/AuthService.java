package com.identity.security.application.service;

import com.identity.common.dto.PagedResponse;
import com.identity.common.dto.UserCredentialsCreated;
import com.identity.common.event.UserCredentialsCreatedEvent;
import com.identity.security.application.dto.*;
import com.identity.security.application.exception.ForbiddenOperationException;
import com.identity.security.application.exception.UserAlreadyExistsException;
import com.identity.security.application.exception.UserNotFoundException;
import com.identity.security.domain.entity.RefreshToken;
import com.identity.security.domain.entity.Role;
import com.identity.security.domain.entity.UserCredentials;
import com.identity.security.infra.persistence.RefreshTokenRepository;
import com.identity.security.infra.persistence.RoleRepository;
import com.identity.security.infra.persistence.UserCredentialsRepository;
import com.identity.security.application.dto.UserCredentialsResponse;
import com.identity.security.application.service.mapper.UserCredentialsMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
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
    private final UserCredentialsMapper userCredentialsMapper;
    private final RoleRepository roleRepository;

    @Value("${jwt.public-key}")
    private RSAPublicKey publicKey;

    public AuthService(UserCredentialsRepository userCredentialsRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, RefreshTokenRepository refreshTokenRepository, ApplicationEventPublisher applicationEventPublisher, UserCredentialsMapper userCredentialsMapper, RoleRepository roleRepository) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userCredentialsMapper = userCredentialsMapper;
        this.roleRepository = roleRepository;
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
        newUserCredentials.setIsActive(true);
        newUserCredentials.setEncryptedPassword(encryptedPassword);
        newUserCredentials.getRoles().add(
                roleRepository.getReferenceById(Role.Value.USER.getId())
        );

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

        return generateTokens(userCredentials.getUserId(), getScopeByRoleList(userCredentials.getRoles()));
    }

    private String getScopeByRoleList(List<Role> roleList){
        StringBuilder stringBuilder = new StringBuilder();

        for (Role role : roleList) {
            stringBuilder.append(role.getName()).append(" ");
        }

        return stringBuilder.toString();
    }

    private LoginResponse generateTokens(UUID userId, String scope){
        TokenResponse accessToken = generateAccessToken(userId, scope);
        TokenResponse refreshToken = generateRefreshToken(userId, scope);

        return new LoginResponse(accessToken, refreshToken);
    }

    private TokenResponse generateAccessToken(UUID userId, String scope){
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS);

        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(userId.toString())
                .claim("token_type", "access")
                .claim("scope", scope)
                .build();

        String jwtValue = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new TokenResponse(jwtValue, expiration);
    }

    private TokenResponse generateRefreshToken(UUID userId, String scope){
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
                .claim("scope", scope)
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

        if (!"refresh".equals(type))
            throw new BadJwtException("Invalid refresh token");

        UserCredentials user = userCredentialsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found with id: "+userId));

        revokeRefreshToken(tokenId);

        return generateTokens(userId, getScopeByRoleList(user.getRoles()));
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

    public void updateUserRole(UUID userId, List<Role.Value> roles) {
        UserCredentials userCredentials = userCredentialsRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found with id: "+userId));

        userCredentials.getRoles().clear();
        for (Role.Value role : roles) {
            userCredentials.getRoles().add(roleRepository.getReferenceById(role.getId()));
        }

        userCredentialsRepository.save(userCredentials);
    }

    public PagedResponse<UserCredentialsResponse> getAllUsers(Pageable pageable) {
        Page<UserCredentials> page = userCredentialsRepository.findAll(pageable);

        return PagedResponse.<UserCredentialsResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(userCredentialsMapper::toResponse)
                        .toList()
                )
                .build();
    }

    public UserCredentialsResponse getUserById(UUID id) {
        return userCredentialsMapper.toResponse(
                userCredentialsRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("user not found with id: "+id))
        );
    }
}
