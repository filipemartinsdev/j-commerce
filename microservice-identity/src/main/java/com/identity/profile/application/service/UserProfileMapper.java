package com.identity.profile.application.service;

import com.identity.profile.application.dto.UserProfileResponse;
import com.identity.profile.domain.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {
    public UserProfileResponse toResponse(UserProfile userProfile){
        return new UserProfileResponse(
                userProfile.getUserId(),
                userProfile.getEmail(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getCreatedAt()
        );
    }
}
