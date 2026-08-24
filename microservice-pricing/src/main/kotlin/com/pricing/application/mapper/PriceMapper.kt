package com.pricing.application.mapper

import com.pricing.application.dto.CreatePriceRequest
import com.pricing.application.dto.PriceResponse
import com.pricing.domain.entity.Price
import com.pricing.domain.entity.PriceType
import java.time.Instant

class PriceMapper {
    fun toResponse(domain: Price): PriceResponse {
        return PriceResponse(
            id = domain.id ?: throw IllegalArgumentException("Price with null id"),
            sku = domain.sku,
            value = domain.value,
            type = PriceResponse.Type(
                id = domain.type.id,
                label = domain.type.label
            ),
            since = domain.since,
            until = domain.until,
            createdAt = domain.createdAt,
            createdBy = domain.createdBy
        )
    }

    fun toDomain(request: CreatePriceRequest): Price {
        return Price(
            sku = request.sku,
            value = request.value,
            type = PriceType.getById(request.typeId),
            since = request.since ?: Instant.now(),
            until = request.until,
            createdBy = request.userId
        )
    }
}