package com.pricing

import com.pricing.application.gateway.MessageProducerGateway
import com.pricing.application.gateway.PriceRepositoryGateway
import com.pricing.application.gateway.ProductRepositoryGateway
import com.pricing.application.mapper.PriceMapper
import com.pricing.application.mapper.ProductMapper
import com.pricing.application.service.PricingEngine
import com.pricing.application.service.PricingEngineImpl
import com.pricing.application.usecase.DeleteProductInteractor
import com.pricing.application.usecase.GetProductsInteractor
import com.pricing.application.usecase.RegisterProductInteractor
import com.pricing.infra.QuarkusUnitOfWork
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Produces

@ApplicationScoped
class Main(
    private val productRepositoryGateway: ProductRepositoryGateway,
    private val priceRepositoryGateway: PriceRepositoryGateway,
    private val messageProducerGateway: MessageProducerGateway,
    private val unitOfWork: QuarkusUnitOfWork,
) {

    @Produces @ApplicationScoped
    fun pricingEngine(): PricingEngine {
        return PricingEngineImpl(
            priceRepositoryGateway = priceRepositoryGateway,
            messageProducerGateway = messageProducerGateway,
            productRepositoryGateway = productRepositoryGateway,
            priceMapper = PriceMapper(),
            unitOfWork = unitOfWork
        )
    }

    @Produces @ApplicationScoped
    fun registerProductInteractor(): RegisterProductInteractor {
        return RegisterProductInteractor(
            productRepositoryGateway,
            unitOfWork = unitOfWork
        )
    }

    @Produces @ApplicationScoped
    fun deleteProductInteractor(): DeleteProductInteractor {
        return DeleteProductInteractor(
            productRepositoryGateway,
            unitOfWork = unitOfWork
        )
    }

    @Produces @ApplicationScoped
    fun getProductsInteractor(): GetProductsInteractor {
        return GetProductsInteractor(
            productRepositoryGateway = productRepositoryGateway,
            productMapper = ProductMapper()
        )
    }
}
