package com.pricing.infra.persistence

import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.domain.entity.Product
import com.pricing.infra.persistence.mapper.ProductMapper
import com.pricing.infra.persistence.model.PriceModel
import com.pricing.infra.persistence.model.ProductModel
import io.github.responsekit.core.PagedResponse
import io.quarkus.hibernate.orm.panache.PanacheQuery
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.LockModeType
import jakarta.transaction.Transactional
import java.util.*

@ApplicationScoped
class ProductRepository(
    val productMapper: ProductMapper
): ProductRepositoryGateway, PanacheRepositoryBase<ProductModel, UUID>{

    @Transactional
    override fun findAll(page: Int, size: Int): PagedResponse<Product> {
        val query: PanacheQuery<ProductModel> = find("deleted IS FALSE")
            .withLock<ProductModel>(LockModeType.PESSIMISTIC_READ)
            .page(page, size)

        return PagedResponse
            .content(query.list<ProductModel>()
                .map(productMapper::toDomain)
                .toList()
            )
            .page(query.page().index.toLong())
            .size(size.toLong())
            .isLast(!query.hasNextPage())
            .totalElements(query.count())
            .totalPages(query.pageCount().toLong())
            .build()
    }

    @Transactional
    override fun findBySku(sku: String): Product? {
        return find("sku = ?1 AND deleted IS FALSE", sku)
                .withLock<ProductModel>(LockModeType.PESSIMISTIC_READ)
                .firstResult<ProductModel>()
                ?.let { productMapper.toDomain(it) }
    }

    @Transactional
    fun findModelBySku(sku: String): ProductModel? {
        return find("sku = ?1 AND deleted IS FALSE", sku)
                .withLock<ProductModel>(LockModeType.PESSIMISTIC_READ)
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