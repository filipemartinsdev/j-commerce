package com.pricing.application.usecase

import com.pricing.application.UnitOfWork
import com.pricing.application.gateway.ProductRepositoryGateway

class DeleteProductInteractor (
    val productRepositoryGateway: ProductRepositoryGateway,
    val unitOfWork: UnitOfWork,
){
    fun deleteProductBySku(sku: String): Boolean {
        return unitOfWork.execute {
            productRepositoryGateway.deleteBySku(sku)
        }
    }
}
