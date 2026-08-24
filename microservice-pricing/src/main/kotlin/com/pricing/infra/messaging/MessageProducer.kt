package com.pricing.infra.messaging

import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.dto.PriceUpdatedMessage
import com.pricing.application.gateway.MessageProducerGateway
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter

@ApplicationScoped
class MessageProducer(
    @field:Channel("price-checked") var priceCheckedEmitter: Emitter<PriceCheckedMessage>,
    @field:Channel("price-updated") var priceUpdatedEmitter: Emitter<PriceUpdatedMessage>
): MessageProducerGateway {

    override fun producePriceChecked(message: PriceCheckedMessage) {
        priceCheckedEmitter.send(message)
    }

    override fun producePriceUpdated(message: PriceUpdatedMessage) {
        priceUpdatedEmitter.send(message)
    }
}