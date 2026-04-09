package com.identity.profile.application.event;

import com.identity.common.event.UserCredentialsCreatedEvent;
import com.identity.profile.application.service.UserProfileService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialsCreatedEventListener {
    private final UserProfileService userProfileService;

    public UserCredentialsCreatedEventListener(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Async
    @EventListener
    public void handleUserCredentialsCreatedEvent(UserCredentialsCreatedEvent userCredentialsCreatedEvent) {
        userProfileService.createFromCredentials(userCredentialsCreatedEvent);
    }
}
