package com.pricing.application.service

import com.pricing.application.dto.CreatePriceRequest
import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.dto.PriceResponse
import java.util.*

interface PricingEngine {
    fun createPrice(request: CreatePriceRequest): PriceResponse

    fun deletePrice(id: UUID)

    fun refreshPrices()

    fun updatePrice(message: PriceCheckedMessage)
}