package com.pricing.application.usecase

import com.pricing.application.dto.RegisterProductRequest
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.domain.entity.Product

class RegisterProductInteractor (
    val productRepositoryGateway: ProductRepositoryGateway
) {
    fun registerProduct(request: RegisterProductRequest) = productRepositoryGateway.save(Product(sku = request.sku))
}
