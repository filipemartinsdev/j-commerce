package com.pricing.application.gateway

import com.pricing.domain.entity.Product

interface ProductRepositoryGateway {
    fun findBySku(sku: String): Product?

    fun save(product: Product): Product

    fun deleteBySku(sku: String): Boolean
}
