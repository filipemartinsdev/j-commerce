package com.pricing.infra.persistence.mapper

import com.pricing.domain.entity.Price
import com.pricing.domain.entity.PriceType
import com.pricing.domain.entity.Product
import com.pricing.infra.persistence.model.PriceModel
import com.pricing.infra.persistence.model.ProductModel
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductMapper {
    fun toDomain(model: ProductModel): Product {
        return Product(
            id = model.id,
            sku = model.sku,
            activePrices = mapActivePrices(model)
        )
    }

    fun toModel(domain: Product): ProductModel {
        val product = ProductModel(sku = domain.sku)
            .apply {
                activePrices = domain.activePrices.map { price ->
                    PriceModel(
                        typeId = price.type.id,
                        value = price.value,
                        product = this,
                        since = price.since,
                        until = price.until,
                        createdBy = price.createdBy
                    )
                }.toMutableList()
        }

        domain.id?.let {
            product.id = it
        }

        return product
    }

    private fun mapActivePrices(model: ProductModel): List<Price> {
        return model.activePrices
            .map { model -> Price(
                id = model.id,
                sku = model.product.sku,
                value = model.value,
                type = PriceType.getById(model.typeId),
                since = model.since,
                until = model.until,
                createdAt = model.createdAt,
                createdBy = model.createdBy,
                active = model.active
            ) }
            .sortedWith(Product.comparePricesByTypeAndCreatedAt)
            .toList()
    }
}
