package com.notification.application.exception;

import io.github.responsekit.core.StandardResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserNotificationHasAlreadyBeenViewedExceptionMapper implements ExceptionMapper<UserNotificationHasAlreadyBeenViewedException> {
    @Override
    public Response toResponse(UserNotificationHasAlreadyBeenViewedException exception) {
        return Response
                .status(404)
                .entity(StandardResponse.fail(exception.getMessage()))
                .build();
    }
}
