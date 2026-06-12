package com.notification.application.exception;

public class UserNotificationNotFoundException extends RuntimeException {
    public UserNotificationNotFoundException(String message) {
        super(message);
    }
}
