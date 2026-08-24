package com.pricing.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreatePriceRequest (
    val sku: String,

    val value: BigDecimal,

    val since: Instant?,

    val until: Instant?,

    val typeId: Int,

    val userId: UUID
)
