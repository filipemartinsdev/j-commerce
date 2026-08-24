package com.pricing.infra.persistence.mapper

import com.pricing.application.exception.ProductNotFoundBySkuException
import com.pricing.domain.entity.Price
import com.pricing.domain.entity.PriceType
import com.pricing.infra.persistence.PriceRepository
import com.pricing.infra.persistence.ProductRepository
import com.pricing.infra.persistence.model.PriceModel
import com.pricing.infra.persistence.model.ProductModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PriceMapper (
    private val productRepository: ProductRepository
){
    fun toDomain(model: PriceModel): Price {
        return Price(
            id = model.id,
            sku = model.product.sku,
            value = model.value,
            type = PriceType.getById(model.typeId),
            since = model.since,
            until = model.until,
            createdAt = model.createdAt,
            createdBy = model.createdBy,
            active = model.active
        )
    }
}
