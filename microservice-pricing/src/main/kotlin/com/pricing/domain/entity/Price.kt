package com.pricing.domain.entity

import java.math.BigDecimal
import java.time.Instant
import java.util.*

class Price(
    var id: UUID? = null,
    val sku: String,
    val value: BigDecimal,
    val type: PriceType,
    val since: Instant = Instant.now(),
    val until: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val createdBy: UUID,
    var active: Boolean = false
){

    fun equals(price: Price): Boolean {
        return price.id?.equals(this.id) ?: false
    }
}