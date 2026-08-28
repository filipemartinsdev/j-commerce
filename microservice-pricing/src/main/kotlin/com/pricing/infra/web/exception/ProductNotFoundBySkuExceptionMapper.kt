package com.pricing.infra.web.exception

import com.pricing.application.exception.ProductNotFoundBySkuException
import io.github.responsekit.core.StandardResponse
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class ProductNotFoundBySkuExceptionMapper: ExceptionMapper<ProductNotFoundBySkuException> {

    override fun toResponse(exception: ProductNotFoundBySkuException): Response {
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(
                StandardResponse
                        .fail()
                        .message(exception.message)
                        .build()
            )
            .build()
    }
}