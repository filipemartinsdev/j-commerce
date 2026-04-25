package com.identity.profile.application.service;

import com.identity.common.event.UserCredentialsCreatedEvent;
import com.identity.profile.application.dto.UserProfileResponse;
import com.identity.profile.application.exception.UserProfileNotFoundException;
import com.identity.profile.application.service.mapper.UserProfileMapper;
import com.identity.profile.domain.entity.UserProfile;
import com.identity.profile.infra.persistence.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileService(UserProfileRepository userProfileRepository, UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    public UserProfileResponse getUserById(UUID authenticatedUserId) {
        return userProfileMapper.toResponse(
                userProfileRepository.findById(authenticatedUserId)
                    .orElseThrow(() -> new UserProfileNotFoundException("User not found with id: "+authenticatedUserId))
        );
    }

    public void createFromCredentials(UserCredentialsCreatedEvent event) {
        UserProfile user = new UserProfile();
        user.setUserId(event.getUser().userId());
        user.setEmail(event.getUser().email());
        user.setFirstName(event.getUser().firstName());
        user.setLastName(event.getUser().lastName());
        user.setIsActive(true);
        userProfileRepository.save(user);
    }
}
