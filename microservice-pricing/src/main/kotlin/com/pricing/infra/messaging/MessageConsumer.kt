package com.pricing.infra.messaging

import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.service.PricingEngine
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming

@ApplicationScoped
class MessageConsumer (
    private val pricingEngine: PricingEngine
){
    @Incoming("product-created")
    fun consumeProductCreated(){}

    @Incoming("product-deleted")
    fun consumeProductDeleted(){}

    @Incoming("apply-price")
    fun consumeApplyPrice(payload: JsonObject){
        val message: PriceCheckedMessage = payload.mapTo(PriceCheckedMessage::class.java)
        pricingEngine.updatePrice(message)
    }
}