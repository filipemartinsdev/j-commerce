package com.pricing.application.gateway

import com.pricing.domain.entity.Price
import java.util.UUID

interface PriceRepositoryGateway {
    fun save(price: Price): Price

    fun findAllBySku(sku: String): List<Price>

    fun deleteById(id: UUID): Boolean

    fun refreshPricesActivityAndRetrieveIt(): List<Price>
}
