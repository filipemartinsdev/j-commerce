package com.pricing.application.usecase

import com.pricing.application.UnitOfWork
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.domain.entity.Product

class RegisterProductInteractor (
    val productRepositoryGateway: ProductRepositoryGateway,
    val unitOfWork: UnitOfWork,
) {
    fun registerProduct(sku: String) {
        return unitOfWork.execute {
            productRepositoryGateway.save(Product(sku = sku))
        }
    }
}
