package com.pricing.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PriceResponse (
    val id: UUID,
    val sku: String,
    val value: BigDecimal,
    val type: Type,
    val since: Instant,
    val until: Instant?,
    val createdAt: Instant,
    val createdBy: UUID
){
    data class Type (val id: Int, val label: String)
}
