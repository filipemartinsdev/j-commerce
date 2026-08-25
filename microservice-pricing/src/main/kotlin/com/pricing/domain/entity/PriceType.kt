package com.pricing.domain.entity

enum class PriceType(val id: Int, val label: String) {
    COMMON(1, "Common"),
    OFFER(2, "Offer"),
    BLACK_FRIDAY(3, "Black Friday");

    companion object {
        fun getById(id: Int): PriceType {
            for (priceType in PriceType.entries)
                if (priceType.id == id) return priceType
            throw IllegalArgumentException("Invalid Price Type with id $id")
        }
    }
}
