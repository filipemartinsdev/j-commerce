package com.pricing.infra.persistence

import com.pricing.application.exception.PriceNotFoundException
import com.pricing.application.exception.ProductNotFoundBySkuException
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.domain.entity.Price
import com.pricing.infra.persistence.mapper.PriceMapper
import com.pricing.infra.persistence.model.PriceModel
import com.pricing.infra.persistence.model.ProductModel
import io.quarkus.hibernate.orm.panache.PanacheQuery
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID
import kotlin.time.Clock.System.now

@ApplicationScoped
class PriceRepository (
    private val priceMapper: PriceMapper,
    private val productRepository: ProductRepository
): PriceRepositoryGateway, PanacheRepositoryBase<PriceModel, UUID>{

    @Transactional
    override fun save(price: Price): Price {
        val product: ProductModel = productRepository.findModelBySku(price.sku)
            ?: throw ProductNotFoundBySkuException(price.sku)

        val newPrice: PriceModel = PriceModel(
            typeId = price.type.id,
            value = price.value,
            product = product,
            since = price.since,
            until = price.until,
            createdBy = price.createdBy
        ).apply {
            price.id?.let { id = it }
        }

        persist(newPrice)

        return priceMapper.toDomain(newPrice)
    }

    override fun findAllBySku(sku: String): List<Price> {
        return find("deleted IS FALSE AND product.sku = ?2", now(), sku)
            .page<PriceModel>(0, 50)
            .list<PriceModel>()
            .map(priceMapper::toDomain)
    }

    override fun findAllForTurnOn(): List<Price>{
        return find("deleted IS FALSE AND since < ?1 AND active IS FALSE", now())
                .page<PriceModel>(0, 50)
                .list<PriceModel>()
                .map(priceMapper::toDomain)
    }

    override fun findAllForTurnOff(): List<Price>{
        return find("deleted IS FALSE AND until < ?1 AND active IS TRUE", now())
            .page<PriceModel>(0, 50)
            .list<PriceModel>()
            .map(priceMapper::toDomain)
    }

    override fun saveAll(prices: List<Price>) {
        if (prices.isEmpty())
            return

        val products: List<ProductModel> = productRepository
            .find("deleted IS FALSE AND sku IN ?1", prices.map(Price::sku))
            .list()

        val productBySku: Map<String, ProductModel> = products.associateBy { it.sku }

        val missingSkus = prices
            .filter { !productBySku.contains(it.sku) }
            .map { it.sku }

        if(missingSkus.isNotEmpty())
            throw ProductNotFoundBySkuException(missingSkus.first())

        val pricesToSave: List<PriceModel> = prices
            .map { domain ->
                PriceModel(
                    typeId = domain.type.id,
                    value = domain.value,
                    product = productBySku.getValue(domain.sku),
                    since = domain.since,
                    until = domain.until,
                    createdBy = domain.createdBy
                ).apply { domain.id?.let { id = it } }
            }

        persist(pricesToSave)
    }
}