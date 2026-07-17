package com.notification.application.exception;

import io.github.responsekit.core.StandardResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserNotificationNotFoundExceptionMapper implements ExceptionMapper<UserNotificationNotFoundException> {
    @Override
    public Response toResponse(UserNotificationNotFoundException exception) {
        return Response
                .status(404)
                .entity(StandardResponse.fail(exception.getMessage()))
                .build();
    }
}
