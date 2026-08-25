package com.pricing.application.service

import com.pricing.application.UnitOfWork
import com.pricing.application.dto.CreatePriceRequest
import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.dto.PriceResponse
import com.pricing.application.dto.PriceUpdatedMessage
import com.pricing.application.exception.PriceNotFoundException
import com.pricing.application.exception.ProductNotFoundBySkuException
import com.pricing.application.exception.ProductWithoutBasePriceException
import com.pricing.application.gateway.MessageProducerGateway
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.application.mapper.PriceMapper
import com.pricing.domain.entity.Price
import com.pricing.domain.entity.PriceType
import java.util.*

class PricingEngineImpl(
    private val priceRepositoryGateway: PriceRepositoryGateway,
    private val messageProducerGateway: MessageProducerGateway,
    private val productRepositoryGateway: ProductRepositoryGateway,
    private val priceMapper: PriceMapper,
    private val unitOfWork: UnitOfWork,
): PricingEngine {

    override fun createPrice(request: CreatePriceRequest): PriceResponse {
        return unitOfWork.execute {
            val product = productRepositoryGateway.findBySku(request.sku)
                ?: throw ProductNotFoundBySkuException(request.sku)

            if (request.typeId != PriceType.COMMON.id)
                product.getBasePrice()
                    ?: throw ProductWithoutBasePriceException("Product without base price: ${request.sku}")

            priceRepositoryGateway
                .save(request.let(priceMapper::toDomain))
                .let(priceMapper::toResponse)
        }
    }

    override fun deletePrice(id: UUID) {
        unitOfWork.execute {
            if (!priceRepositoryGateway.deleteById(id))
                throw PriceNotFoundException("Price not found by id: $id")
        }
    }

    override fun getPricesBySku(sku: String): List<PriceResponse> {
        return unitOfWork.execute {
            priceRepositoryGateway.findAllBySku(sku)
                .map(priceMapper::toResponse)
        }
    }

    override fun refreshPrices() {
        unitOfWork.execute unit@ {
            val refreshedPrices:List<Price> = priceRepositoryGateway.refreshPricesActivityAndRetrieveIt()

            if (refreshedPrices.isEmpty())
                return@unit

            refreshedPrices.forEach {
                producePriceChecked(it)
            }
        }
    }

    private fun producePriceChecked(price: Price){
        messageProducerGateway.producePriceChecked(PriceCheckedMessage(price.sku))
    }


    override fun updatePrice(message: PriceCheckedMessage) {
        unitOfWork.execute {
            val product = productRepositoryGateway.findBySku(message.sku)
                ?: throw ProductNotFoundBySkuException("Product not found by SKU: " + message.sku)

            producePriceUpdated(
                sku = product.sku,
                newBasePrice = product.getBasePrice(),
                newCurrentPrice = product.getCurrentPrice()
            )
        }
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