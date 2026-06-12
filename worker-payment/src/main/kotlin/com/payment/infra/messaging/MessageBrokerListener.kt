package com.payment.infra.messaging

import com.payment.application.message.GeneratePaymentMessage
import com.payment.application.service.PaymentService
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming

@ApplicationScoped
class MessageBrokerListener(
    private val paymentService: PaymentService,
) {

    @Incoming("generate-payment")
    fun consumeGeneratePayment(payload: JsonObject) {
        val message: GeneratePaymentMessage = payload.mapTo(GeneratePaymentMessage::class.java)
        this.paymentService.generatePayment(message)
    }
}
