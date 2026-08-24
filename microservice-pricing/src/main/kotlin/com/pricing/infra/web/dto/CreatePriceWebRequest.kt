package com.pricing.infra.web.dto

import java.math.BigDecimal
import java.time.Instant

data class CreatePriceWebRequest (
    val sku: String,

    val value: BigDecimal,

    val since: Instant?,

    val until: Instant?,

    val typeId: Int,
)