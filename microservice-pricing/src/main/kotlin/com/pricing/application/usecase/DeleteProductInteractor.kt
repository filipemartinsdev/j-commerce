package com.pricing.application.usecase

import com.pricing.application.gateway.ProductRepositoryGateway

class DeleteProductInteractor (
    val productRepositoryGateway: ProductRepositoryGateway
){
    fun deleteProductBySku(sku: String): Boolean = productRepositoryGateway.deleteBySku(sku)
}
