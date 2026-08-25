package com.pricing.application.mapper

import com.pricing.application.dto.ProductResponse
import com.pricing.domain.entity.Product

class ProductMapper {
    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id ?: throw IllegalArgumentException("Product ID is required"),
            sku = product.sku
        )
    }
}