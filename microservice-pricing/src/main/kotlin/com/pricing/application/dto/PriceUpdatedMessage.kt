package com.pricing.application.dto

import java.math.BigDecimal

data class PriceUpdatedMessage(
    val sku: String,
    val basePrice: PriceMessage?,
    val currentPrice: PriceMessage?,
){
    data class PriceMessage (val label: String, val value: BigDecimal)
}
