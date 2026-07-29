package com.payment.infra.messaging

import com.payment.application.message.PaymentGeneratedMessage
import com.payment.application.message.RefundPaymentMessage
import com.payment.application.message.GeneratePaymentMessage
import com.payment.application.service.PaymentService
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.reactive.messaging.Incoming
import java.time.temporal.ChronoUnit

@ApplicationScoped
class MessageBrokerListener(
    private val paymentService: PaymentService,
) {

    @Incoming("generate-payment")
    @Retry(maxRetries = 3, delay = 500, delayUnit = ChronoUnit.MILLIS)
    fun consumeGeneratePayment(payload: JsonObject) {
        val message: GeneratePaymentMessage = payload.mapTo(GeneratePaymentMessage::class.java)
        this.paymentService.generatePayment(message)
    }

    @Incoming("handle-payment-timeout")
    @Retry(maxRetries = 3, delay = 500, delayUnit = ChronoUnit.MILLIS)
    fun consumeHandlePaymentTimeout(payload: JsonObject) {
        val message: PaymentGeneratedMessage = payload.mapTo(PaymentGeneratedMessage::class.java)
        this.paymentService.handlePaymentTimeout(message)
    }

    @Incoming("refund-payment")
    @Retry(maxRetries = 3, delay = 500, delayUnit = ChronoUnit.MILLIS)
    fun consumePaymentRefund(payload: JsonObject) {
        val message: RefundPaymentMessage = payload.mapTo(RefundPaymentMessage::class.java)
        this.paymentService.refundPayment(message)
    }
}
