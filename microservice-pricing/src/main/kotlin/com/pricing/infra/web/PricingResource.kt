package com.pricing.infra.web

import com.pricing.application.dto.CreatePriceRequest
import com.pricing.application.service.PricingEngine
import com.pricing.infra.web.dto.CreatePriceWebRequest
import com.pricing.infra.web.dto.PriceWebResponse
import com.pricing.infra.web.mapper.PriceMapper
import io.github.responsekit.core.StandardResponse
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.RestResponse.Status
import java.util.*

@Path("/api/v1/admin/prices")
@Produces(MediaType.APPLICATION_JSON)
class PricingResource(
    private val pricingEngine: PricingEngine,
    private val priceMapper: PriceMapper,
    private val jwt: JsonWebToken
) {

    @GET
    @RolesAllowed("ADMIN")
    fun getPrices(@RestQuery sku: String): RestResponse<StandardResponse<List<PriceWebResponse>>>{
        return RestResponse.ok(
            StandardResponse.success(
                pricingEngine.getPricesBySku(sku)
                    .map(priceMapper::toWebResponse)
            ).build()
        )
    }

    @POST
    @RolesAllowed("ADMIN")
    fun create(@RequestBody request: CreatePriceWebRequest): RestResponse<PriceWebResponse> {
        val authenticatedUserId = UUID.fromString(jwt.subject)

        return RestResponse.status(Status.CREATED,
            pricingEngine.createPrice(CreatePriceRequest(
                sku = request.sku,
                value = request.value,
                since = request.since,
                until = request.until,
                typeId = request.typeId,
                userId = authenticatedUserId
            )).let { priceMapper.toWebResponse(it) }
        )
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    fun delete(@RestPath id: UUID): RestResponse<Unit> {
        pricingEngine.deletePrice(id)
        return RestResponse.ok()
    }
}