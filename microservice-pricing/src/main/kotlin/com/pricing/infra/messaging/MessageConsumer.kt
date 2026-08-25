package com.pricing.infra.messaging

import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.dto.ProductCreatedMessage
import com.pricing.application.dto.ProductDeletedMessage
import com.pricing.application.service.PricingEngine
import com.pricing.application.usecase.DeleteProductInteractor
import com.pricing.application.usecase.RegisterProductInteractor
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming

@ApplicationScoped
class MessageConsumer (
    private val pricingEngine: PricingEngine,
    private val registerProductInteractor: RegisterProductInteractor,
    private val deleteProductInteractor: DeleteProductInteractor,
){
    @Incoming("product-created")
    fun consumeProductCreated(payload: JsonObject){
        val message: ProductCreatedMessage = payload.mapTo(ProductCreatedMessage::class.java)
        registerProductInteractor.registerProduct(message.sku)
    }

    @Incoming("product-deleted")
    fun consumeProductDeleted(payload: JsonObject){
        val message: ProductDeletedMessage = payload.mapTo(ProductDeletedMessage::class.java)
        deleteProductInteractor.deleteProductBySku(message.sku)
    }

    @Incoming("apply-price")
    fun consumeApplyPrice(payload: JsonObject){
        val message: PriceCheckedMessage = payload.mapTo(PriceCheckedMessage::class.java)
        pricingEngine.updatePrice(message)
    }
}