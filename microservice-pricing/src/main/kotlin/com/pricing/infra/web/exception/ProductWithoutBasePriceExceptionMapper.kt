package com.pricing.infra.web.exception

import com.pricing.application.exception.ProductWithoutBasePriceException
import io.github.responsekit.core.StandardResponse
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class ProductWithoutBasePriceExceptionMapper: ExceptionMapper<ProductWithoutBasePriceException> {

    override fun toResponse(exception: ProductWithoutBasePriceException): Response {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(
                StandardResponse
                        .fail()
                        .message(exception.message)
                        .build()
            )
            .build()
    }
}