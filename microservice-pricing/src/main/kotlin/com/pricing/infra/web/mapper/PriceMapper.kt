package com.pricing.infra.web.mapper

import com.pricing.application.dto.PriceResponse
import com.pricing.infra.web.dto.PriceWebResponse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PriceMapper {
    fun toWebResponse(response: PriceResponse): PriceWebResponse {
        return PriceWebResponse(
            id = response.id,
            sku = response.sku,
            value = response.value,
            type = PriceWebResponse.Type(
                id = response.type.id,
                label = response.type.label
            ),
            since = response.since,
            until = response.until,
            createdAt = response.createdAt,
            createdBy = response.createdBy
        )
    }
}