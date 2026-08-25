package com.pricing.infra.persistence

import com.pricing.application.exception.ProductNotFoundBySkuException
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.domain.entity.Price
import com.pricing.infra.persistence.mapper.PriceMapper
import com.pricing.infra.persistence.model.PriceModel
import com.pricing.infra.persistence.model.ProductModel
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import java.time.Instant
import java.util.*

@ApplicationScoped
class PriceRepository (
    private val priceMapper: PriceMapper,
    private val productRepository: ProductRepository,
    private val entityManager: EntityManager
): PriceRepositoryGateway, PanacheRepositoryBase<PriceModel, UUID>{

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

        newPrice.persist()

        return priceMapper.toDomain(newPrice)
    }

    override fun findAllBySku(sku: String): List<Price> {
        return find("deleted IS FALSE AND product.sku = ?1", sku)
            .withLock<PriceModel>(LockModeType.PESSIMISTIC_READ)
            .page<PriceModel>(0, 50)
            .list<PriceModel>()
            .map(priceMapper::toDomain)
    }

    override fun refreshPricesActivityAndRetrieveIt(): List<Price> {
        val pricesForTurnOn: List<PriceModel> = findAllForTurnOn()
        val pricesForTurnOff: List<PriceModel> = findAllForTurnOff()

        if (pricesForTurnOn.isEmpty() && pricesForTurnOff.isEmpty())
            return listOf()

        pricesForTurnOn.forEach {
            it.active = true
        }
        pricesForTurnOff.forEach {
            it.active = false
        }

        val pricesForSave: List<PriceModel> = mutableListOf<PriceModel>()
            .apply {
                addAll(pricesForTurnOn)
                addAll(pricesForTurnOff)
            }

        persist(pricesForSave)

        return pricesForSave.map(priceMapper::toDomain)
    }

    private fun findAllForTurnOn(): List<PriceModel>{
        return find("deleted IS FALSE AND since <= ?1 AND active IS FALSE", Instant.now())
            .withLock<PriceModel>(LockModeType.PESSIMISTIC_READ)
            .page<PriceModel>(0, 50)
            .list()
    }

    private fun findAllForTurnOff(): List<PriceModel>{
        return find("deleted IS FALSE AND until <= ?1 AND active IS TRUE", Instant.now())
            .withLock<PriceModel>(LockModeType.PESSIMISTIC_READ)
            .page<PriceModel>(0, 50)
            .list()
    }
}