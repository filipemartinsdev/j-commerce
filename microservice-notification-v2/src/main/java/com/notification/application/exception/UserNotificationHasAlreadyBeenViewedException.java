package com.notification.application.exception;

public class UserNotificationHasAlreadyBeenViewedException extends RuntimeException {
    public UserNotificationHasAlreadyBeenViewedException(String message) {
        super(message);
    }
}
