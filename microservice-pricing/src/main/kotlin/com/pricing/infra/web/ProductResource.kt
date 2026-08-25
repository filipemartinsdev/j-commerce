package com.pricing.infra.web

import com.pricing.application.dto.ProductResponse
import com.pricing.application.usecase.GetProductsInteractor
import io.github.responsekit.core.PagedResponse
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.inject.Default
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse

@Produces(MediaType.APPLICATION_JSON)
@Path("/api/v1/admin/products")
class ProductResource(
    private val getProductsInteractor: GetProductsInteractor
) {

    @GET
    @RolesAllowed("ADMIN")
    fun getProducts(
        @RestQuery @DefaultValue("0") page: Int,
        @RestQuery @DefaultValue("20") size: Int
    ): RestResponse<PagedResponse<ProductResponse>> {
        return RestResponse.ok(
            getProductsInteractor.getProducts(page, size)
        )
    }
}