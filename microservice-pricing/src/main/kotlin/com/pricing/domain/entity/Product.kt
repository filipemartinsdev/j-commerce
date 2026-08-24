package com.pricing.domain.entity

import java.util.UUID

class Product (
    var id: UUID? = null,
    var sku: String,
    val activePrices: List<Price> = listOf()
) {
    companion object {
        val comparePricesByTypeAndCreatedAt: Comparator<Price> = { price1, price2 ->
            if (price1.type.id > price2.type.id) 1
            else if (price1.type.id < price2.type.id) -1
            else if (price1.createdAt.isAfter(price2.createdAt)) 1
            else if (price1.createdAt.isBefore(price2.createdAt)) -1
            else 0
        }
    }

    fun getCurrentPrice(): Price? {
        return activePrices
            .sortedWith(comparePricesByTypeAndCreatedAt)
            .lastOrNull()
    }

    fun getBasePrice(): Price? {
        return activePrices
            .filter { price -> price.type.id == PriceType.COMMON.id }
            .sortedBy { price -> price.createdAt }
            .lastOrNull()
    }
}
