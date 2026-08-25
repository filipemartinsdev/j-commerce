package com.pricing.application.usecase

import com.pricing.application.dto.ProductResponse
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.application.mapper.ProductMapper
import com.pricing.domain.entity.Product
import io.github.responsekit.core.PagedResponse

class GetProductsInteractor(
    val productRepositoryGateway: ProductRepositoryGateway,
    val productMapper: ProductMapper,
) {

    fun getProducts(page: Int, size: Int): PagedResponse<ProductResponse> {
        val products: PagedResponse<Product> = productRepositoryGateway.findAll(page, size)

        return PagedResponse
            .content(products.content.map(productMapper::toResponse))
            .page(products.page)
            .size(products.size)
            .isLast(products.isLast)
            .totalElements(products.totalElements)
            .totalPages(products.totalPages)
            .build()
    }
}