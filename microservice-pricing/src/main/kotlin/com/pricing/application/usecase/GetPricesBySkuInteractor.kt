package com.pricing.application.usecase

import com.pricing.application.dto.PriceResponse
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.application.mapper.PriceMapper

class GetPricesBySkuInteractor (
    private val priceRepositoryGateway: PriceRepositoryGateway,
    private val priceMapper: PriceMapper,
) {
    fun getPricesBySku(sku: String): List<PriceResponse> {
        return priceRepositoryGateway.findAllBySku(sku)
                .map(priceMapper::toResponse)
    }
}