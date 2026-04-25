package com.identity.security.application.service;

import com.identity.common.dto.PagedResponse;
import com.identity.common.event.UserCredentialsCreatedEvent;
import com.identity.security.application.dto.*;
import com.identity.security.application.exception.ForbiddenOperationException;
import com.identity.security.application.exception.UserAlreadyExistsException;
import com.identity.security.application.exception.UserNotFoundException;
import com.identity.security.application.service.mapper.UserCredentialsMapper;
import com.identity.security.domain.entity.Role;
import com.identity.security.domain.entity.RefreshToken;
import com.identity.security.domain.entity.UserCredentials;
import com.identity.security.infra.persistence.RefreshTokenRepository;
import com.identity.security.infra.persistence.RoleRepository;
import com.identity.security.infra.persistence.UserCredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private UserCredentialsMapper userCredentialsMapper;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setupPublicKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        ReflectionTestUtils.setField(authService, "publicKey", keyPair.getPublic());
    }

    @Test
    @DisplayName("Should register users successfully if everything is OK")
    void registerTestCase1() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe"
        );

        UserCredentials newUserCredentials = new UserCredentials();
        newUserCredentials.setUserId(UUID.randomUUID());
        newUserCredentials.setEmail("test@example.com");
        newUserCredentials.setFirstName("John");
        newUserCredentials.setLastName("Doe");
        newUserCredentials.setIsActive(true);
        newUserCredentials.setEncryptedPassword("encodedPassword");
        newUserCredentials.setRoles(new ArrayList<>());

        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");

        when(userCredentialsRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");
        when(roleRepository.getReferenceById(1))
                .thenReturn(userRole);
        when(userCredentialsRepository.save(any(UserCredentials.class)))
                .thenReturn(newUserCredentials);

        authService.register(request);

        verify(userCredentialsRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(roleRepository).getReferenceById(1);
        verify(userCredentialsRepository).save(any(UserCredentials.class));
        verify(applicationEventPublisher).publishEvent(any(UserCredentialsCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException if email is already in user")
    void registerTestCase2() {
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                "John",
                "Doe"
        );

        when(userCredentialsRepository.existsByEmail("existing@example.com"))
                .thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request);
        });

        verify(userCredentialsRepository).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userCredentialsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should login successfully if everything is OK")
    void loginTestCase1() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "password123"
        );

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserId(UUID.randomUUID());
        userCredentials.setEmail("test@example.com");
        userCredentials.setEncryptedPassword("encodedPassword");
        userCredentials.setFirstName("John");
        userCredentials.setLastName("Doe");
        userCredentials.setIsActive(true);
        userCredentials.setRoles(new ArrayList<>());

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");
        userCredentials.getRoles().add(adminRole);

        when(userCredentialsRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(userCredentials));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(mock(Jwt.class));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken token = invocation.getArgument(0);
                    token.setId(UUID.randomUUID());
                    return token;
                });

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        verify(userCredentialsRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user not exists by email")
    void loginTestCase2() {
        LoginRequest request = new LoginRequest(
                "notfound@example.com",
                "password123"
        );

        when(userCredentialsRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            authService.login(request);
        });

        verify(userCredentialsRepository).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("Should throw ForbiddenOperationException if login credential is wrong")
    void loginTestCase3() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "wrongPassword"
        );

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("test@example.com");
        userCredentials.setEncryptedPassword("encodedPassword");

        when(userCredentialsRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(userCredentials));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> {
            authService.login(request);
        });

        verify(userCredentialsRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    @DisplayName("Should refresh successfully if everything is OK")
    void refreshTestCase1() {
        RefreshRequest request = new RefreshRequest("validRefreshToken");

        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn(tokenId.toString());
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserId(userId);
        userCredentials.setEmail("test@example.com");
        userCredentials.setFirstName("John");
        userCredentials.setLastName("Doe");
        userCredentials.setRoles(new ArrayList<>());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(tokenId);
        refreshToken.setUserId(userId);
        refreshToken.setIsRevoked(false);

        when(jwtDecoder.decode("validRefreshToken"))
                .thenReturn(jwt);
        when(userCredentialsRepository.findById(userId))
                .thenReturn(Optional.of(userCredentials));
        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(refreshToken));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(mock(Jwt.class));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken token = invocation.getArgument(0);
                    token.setId(UUID.randomUUID());
                    return token;
                });

        LoginResponse result = authService.refresh(request);

        assertNotNull(result);
        verify(jwtDecoder).decode("validRefreshToken");
        verify(userCredentialsRepository).findById(userId);
        verify(refreshTokenRepository).findById(tokenId);
    }

    @Test
    @DisplayName("Should throw BadJwtException if refresh token is invalid")
    void refreshTestCase2() {
        RefreshRequest request = new RefreshRequest("invalidToken");

        when(jwtDecoder.decode("invalidToken"))
                .thenThrow(new BadJwtException("Invalid token"));

        assertThrows(BadJwtException.class, () -> {
            authService.refresh(request);
        });

        verify(jwtDecoder).decode("invalidToken");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user not exists by ID")
    void refreshTestCase3() {
        RefreshRequest request = new RefreshRequest("validRefreshToken");

        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn(tokenId.toString());
        when(jwt.getSubject()).thenReturn(userId.toString());
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");

        when(jwtDecoder.decode("validRefreshToken"))
                .thenReturn(jwt);
        when(userCredentialsRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            authService.refresh(request);
        });

        verify(jwtDecoder).decode("validRefreshToken");
        verify(userCredentialsRepository).findById(userId);
    }

    @Test
    @DisplayName("Should retrieve the public JWKS successfully")
    void getPublicJWKSTestCase1() {
        var result = authService.getPublicJWKS();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should update user role successfully if everything is OK")
    void updateUserRoleTestCase1() {
        UUID userId = UUID.randomUUID();
        List<Role.Value> roles = List.of(Role.Value.ADMIN);

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserId(userId);
        userCredentials.setEmail("test@example.com");
        userCredentials.setRoles(new ArrayList<>());

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");

        when(userCredentialsRepository.findById(userId))
                .thenReturn(Optional.of(userCredentials));
        when(roleRepository.getReferenceById(2))
                .thenReturn(adminRole);
        when(userCredentialsRepository.save(any(UserCredentials.class)))
                .thenReturn(userCredentials);

        authService.updateUserRole(userId, roles);

        assertEquals(1, userCredentials.getRoles().size());
        verify(userCredentialsRepository).findById(userId);
        verify(roleRepository).getReferenceById(2);
        verify(userCredentialsRepository).save(userCredentials);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user not exists by ID")
    void updateUserRoleTestCase2() {
        UUID userId = UUID.randomUUID();
        List<Role.Value> roles = List.of(Role.Value.ADMIN);

        when(userCredentialsRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            authService.updateUserRole(userId, roles);
        });

        verify(userCredentialsRepository).findById(userId);
        verify(roleRepository, never()).getReferenceById(any());
    }

    @Test
    @DisplayName("Should retrieve all users successfully")
    void getAllUsersTestCase1() {
        Pageable pageable = PageRequest.of(0, 10);

        UserCredentials user1 = new UserCredentials();
        user1.setUserId(UUID.randomUUID());
        user1.setEmail("user1@test.com");

        UserCredentials user2 = new UserCredentials();
        user2.setUserId(UUID.randomUUID());
        user2.setEmail("user2@test.com");

        Page<UserCredentials> page = new PageImpl<>(
                List.of(user1, user2),
                pageable,
                2
        );

        UserCredentialsResponse response1 = new UserCredentialsResponse(
                user1.getUserId(),
                "user1@test.com",
                "First",
                "User",
                List.of(),
                Instant.now()
        );

        UserCredentialsResponse response2 = new UserCredentialsResponse(
                user2.getUserId(),
                "user2@test.com",
                "Second",
                "User",
                List.of(),
                Instant.now()
        );

        when(userCredentialsRepository.findAll(pageable))
                .thenReturn(page);
        when(userCredentialsMapper.toResponse(user1))
                .thenReturn(response1);
        when(userCredentialsMapper.toResponse(user2))
                .thenReturn(response2);

        PagedResponse<UserCredentialsResponse> result = authService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(2, result.content().size());

        verify(userCredentialsRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should return empty PagedResponse if not exists active users")
    void getAllUsersTestCase2() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserCredentials> emptyPage = new PageImpl<>(
                List.of(),
                pageable,
                0
        );

        when(userCredentialsRepository.findAll(pageable))
                .thenReturn(emptyPage);

        PagedResponse<UserCredentialsResponse> result = authService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());

        verify(userCredentialsRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should retrieve user by ID successfully if everything is OK")
    void getUserByIdTestCase1() {
        UUID userId = UUID.randomUUID();

        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserId(userId);
        userCredentials.setEmail("test@example.com");
        userCredentials.setFirstName("John");
        userCredentials.setLastName("Doe");

        UserCredentialsResponse response = new UserCredentialsResponse(
                userId,
                "test@example.com",
                "John",
                "Doe",
                List.of(),
                Instant.now()
        );

        when(userCredentialsRepository.findById(userId))
                .thenReturn(Optional.of(userCredentials));
        when(userCredentialsMapper.toResponse(userCredentials))
                .thenReturn(response);

        UserCredentialsResponse result = authService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals("test@example.com", result.email());

        verify(userCredentialsRepository).findById(userId);
        verify(userCredentialsMapper).toResponse(userCredentials);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user not exits by ID")
    void getUserByIdTestCase2() {
        UUID userId = UUID.randomUUID();

        when(userCredentialsRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            authService.getUserById(userId);
        });

        verify(userCredentialsRepository).findById(userId);
    }
}