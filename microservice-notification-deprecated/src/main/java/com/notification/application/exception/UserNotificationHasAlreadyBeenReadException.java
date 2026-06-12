package com.notification.application.exception;

public class UserNotificationHasAlreadyBeenReadException extends RuntimeException {
    public UserNotificationHasAlreadyBeenReadException(String message) {
        super(message);
    }
}
