package com.pricing.application.dto

import java.util.UUID

data class ProductResponse (
    val id: UUID,
    val sku: String
) {
}