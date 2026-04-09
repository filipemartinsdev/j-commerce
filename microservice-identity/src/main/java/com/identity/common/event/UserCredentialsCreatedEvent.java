package com.identity.common.event;

import com.identity.common.dto.UserCredentialsCreated;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserCredentialsCreatedEvent extends ApplicationEvent {
    private final UserCredentialsCreated user;

    public UserCredentialsCreatedEvent(UserCredentialsCreated user, Object source) {
        super(source);
        this.user = user;
    }
}
