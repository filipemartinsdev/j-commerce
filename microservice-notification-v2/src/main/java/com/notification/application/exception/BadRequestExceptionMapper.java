package com.notification.application.exception;

import io.github.responsekit.core.StandardResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {
    @Override
    public Response toResponse(BadRequestException exception) {
        return Response
                .status(400)
                .entity(StandardResponse.fail(exception.getMessage()))
                .build();
    }
}
