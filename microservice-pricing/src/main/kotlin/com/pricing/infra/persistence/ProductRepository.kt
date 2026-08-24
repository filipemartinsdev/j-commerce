package com.pricing.infra.persistence

import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.domain.entity.Product
import com.pricing.infra.persistence.mapper.ProductMapper
import com.pricing.infra.persistence.model.ProductModel
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class ProductRepository(
    val productMapper: ProductMapper
): ProductRepositoryGateway, PanacheRepositoryBase<ProductModel, UUID>{

    override fun findBySku(sku: String): Product? {
        return find("sku = ?1 AND deleted IS FALSE", sku)
                .firstResult<ProductModel>()
                ?.let { productMapper.toDomain(it) }
    }

    fun findModelBySku(sku: String): ProductModel? {
        return find("sku = ?1 AND deleted IS FALSE", sku)
                .firstResult()
    }

    @Transactional
    override fun save(product: Product): Product {
        return productMapper.toModel(product)
                .let { persist(it); it }
                .let { productMapper.toDomain(it) }
    }

    @Transactional
    override fun deleteBySku(sku: String): Boolean {
        return find("sku = ?1 AND deleted IS FALSE", sku)
                .firstResult<ProductModel>()
                ?.let { delete(it); true }
                ?: false
    }
}