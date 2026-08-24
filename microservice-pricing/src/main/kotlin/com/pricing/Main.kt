package com.pricing

import com.pricing.application.gateway.MessageProducerGateway
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.application.mapper.PriceMapper
import com.pricing.application.service.PricingEngine
import com.pricing.application.service.PricingEngineImpl
import com.pricing.infra.messaging.MessageProducer
import com.pricing.infra.persistence.PriceRepository
import com.pricing.infra.persistence.ProductRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Produces

@ApplicationScoped
class Main(
    private val productRepositoryGateway: ProductRepositoryGateway,
    private val priceRepositoryGateway: PriceRepositoryGateway,
    private val messageProducerGateway: MessageProducerGateway
) {

    @Produces @ApplicationScoped
    fun pricingEngine(): PricingEngine {
        return PricingEngineImpl(
            priceRepositoryGateway = priceRepositoryGateway,
            messageProducerGateway = messageProducerGateway,
            productRepositoryGateway = productRepositoryGateway,
            priceMapper = PriceMapper()
        )
    }
}
