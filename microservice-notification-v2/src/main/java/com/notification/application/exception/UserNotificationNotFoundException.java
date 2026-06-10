package com.notification.application.exception;

import jakarta.ws.rs.NotFoundException;

public class UserNotificationNotFoundException extends NotFoundException {
    public UserNotificationNotFoundException(String message) {
        super(message);
    }
}
