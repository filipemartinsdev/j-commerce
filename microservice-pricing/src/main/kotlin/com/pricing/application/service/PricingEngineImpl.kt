package com.pricing.application.service

import com.pricing.application.dto.CreatePriceRequest
import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.dto.PriceResponse
import com.pricing.application.dto.PriceUpdatedMessage
import com.pricing.application.exception.PriceNotFoundException
import com.pricing.application.exception.ProductNotFoundBySkuException
import com.pricing.application.gateway.MessageProducerGateway
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.application.mapper.PriceMapper
import com.pricing.domain.entity.Price
import java.util.*

class PricingEngineImpl(
    private val priceRepositoryGateway: PriceRepositoryGateway,
    private val messageProducerGateway: MessageProducerGateway,
    private val productRepositoryGateway: ProductRepositoryGateway,
    private val priceMapper: PriceMapper,
): PricingEngine {

    override fun createPrice(request: CreatePriceRequest): PriceResponse {
        return priceRepositoryGateway
            .save(request.let(priceMapper::toDomain))
            .let(priceMapper::toResponse)
    }

    override fun deletePrice(id: UUID) {
        if (!priceRepositoryGateway.deleteById(id))
            throw PriceNotFoundException("Price not found by id: $id")
    }

    override fun refreshPrices() {
        val pricesForTurnOff: List<Price> = priceRepositoryGateway.findAllForTurnOff()
        val pricesForTurnOn: List<Price> = priceRepositoryGateway.findAllForTurnOn()

        if (pricesForTurnOn.isEmpty() && pricesForTurnOff.isEmpty()) return

        for (price in pricesForTurnOff)
            price.active = false

        for (price in pricesForTurnOn)
            price.active = true

        val pricesForUpdate: MutableList<Price> = ArrayList<Price>()
                .apply { addAll(pricesForTurnOff) }
                .apply { addAll(pricesForTurnOn) }

        priceRepositoryGateway.saveAll(pricesForUpdate)
        producePriceChecked(pricesForUpdate)
    }

    private fun producePriceChecked(prices: List<Price>){
        prices.forEach {
            messageProducerGateway.producePriceChecked(PriceCheckedMessage(it.sku))
        }
    }


    override fun updatePrice(message: PriceCheckedMessage) {
        val product = productRepositoryGateway.findBySku(message.sku)
            ?: throw ProductNotFoundBySkuException("Product not found by SKU: " + message.sku)

        producePriceUpdated(
            sku = product.sku,
            newBasePrice = product.getBasePrice(),
            newCurrentPrice = product.getCurrentPrice()
        )
    }

    private fun producePriceUpdated(
        sku: String,
        newBasePrice: Price?,
        newCurrentPrice: Price?
    ) {
        messageProducerGateway.producePriceUpdated(PriceUpdatedMessage(
            sku = sku,
            basePrice = newBasePrice?.let {
                PriceUpdatedMessage.PriceMessage(it.type.label, it.value)
            },
            currentPrice = newCurrentPrice?.let {
                PriceUpdatedMessage.PriceMessage(it.type.label, it.value)
            }
        ))
    }
}