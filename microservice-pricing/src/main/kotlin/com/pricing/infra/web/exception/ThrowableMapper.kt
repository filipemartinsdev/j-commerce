package com.pricing.infra.web.exception

import io.github.responsekit.core.StandardResponse
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@Provider
class ThrowableMapper: ExceptionMapper<Throwable> {
    private val log: Logger = LoggerFactory.getLogger(ThrowableMapper::class.java)

    override fun toResponse(exception: Throwable): Response {
        log.error(exception.message, exception)

        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(
                StandardResponse
                        .fail()
                        .build()
            )
            .build()
    }
}