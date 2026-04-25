package com.identity.profile.application.service;

import com.identity.common.event.UserCredentialsCreatedEvent;
import com.identity.common.dto.UserCredentialsCreated;
import com.identity.profile.application.dto.UserProfileResponse;
import com.identity.profile.application.exception.UserProfileNotFoundException;
import com.identity.profile.application.service.mapper.UserProfileMapper;
import com.identity.profile.domain.entity.UserProfile;
import com.identity.profile.infra.persistence.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    @DisplayName("Should retrieve user profile successfully if everything is OK")
    void getUserByIdTestCase1() {
        UUID userId = UUID.randomUUID();

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setEmail("test@example.com");
        userProfile.setFirstName("John");
        userProfile.setLastName("Doe");
        userProfile.setIsActive(true);

        UserProfileResponse response = new UserProfileResponse(
                userId,
                "test@example.com",
                "John",
                "Doe",
                Instant.now()
        );

        when(userProfileRepository.findById(userId))
                .thenReturn(Optional.of(userProfile));
        when(userProfileMapper.toResponse(userProfile))
                .thenReturn(response);

        UserProfileResponse result = userProfileService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals("test@example.com", result.email());
        verify(userProfileRepository).findById(userId);
        verify(userProfileMapper).toResponse(userProfile);
    }

    @Test
    @DisplayName("Should throw UserProfileNotFoundException if user not found")
    void getUserByIdTestCase2() {
        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> {
            userProfileService.getUserById(userId);
        });

        verify(userProfileRepository).findById(userId);
    }

    @Test
    @DisplayName("Should create user profile from credentials event")
    void createFromCredentialsTestCase1() {
        UUID userId = UUID.randomUUID();
        UserCredentialsCreated userCredentials = new UserCredentialsCreated(
                userId,
                "test@example.com",
                "John",
                "Doe",
                Instant.now()
        );

        UserCredentialsCreatedEvent event = new UserCredentialsCreatedEvent(userCredentials, this);

        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userProfileService.createFromCredentials(event);

        verify(userProfileRepository).save(any(UserProfile.class));
    }
}