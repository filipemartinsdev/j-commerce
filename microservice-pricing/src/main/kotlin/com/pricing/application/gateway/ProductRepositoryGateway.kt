package com.pricing.application.gateway

import com.pricing.domain.entity.Product
import io.github.responsekit.core.PagedResponse

interface ProductRepositoryGateway {
    fun findAll(page: Int, size: Int): PagedResponse<Product>

    fun findBySku(sku: String): Product?

    fun save(product: Product): Product

    fun deleteBySku(sku: String): Boolean
}
