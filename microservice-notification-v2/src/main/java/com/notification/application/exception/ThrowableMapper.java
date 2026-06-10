package com.notification.application.exception;

import io.github.responsekit.core.StandardResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {
    private static final Logger log = LoggerFactory.getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        log.error(exception.getMessage(), exception);

        return Response
                .status(500)
                .entity(StandardResponse.error().build())
                .build();
    }
}
