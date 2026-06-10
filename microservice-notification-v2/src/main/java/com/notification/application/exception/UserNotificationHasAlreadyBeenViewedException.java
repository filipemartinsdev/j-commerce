package com.notification.application.exception;

import jakarta.ws.rs.BadRequestException;

public class UserNotificationHasAlreadyBeenViewedException extends BadRequestException {
    public UserNotificationHasAlreadyBeenViewedException(String message) {
        super(message);
    }
}
