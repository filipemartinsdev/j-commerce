package com.payment.application.service

import com.payment.application.message.GeneratePaymentMessage
import com.payment.application.message.PaymentConfirmedMessage
import com.payment.application.message.PaymentGeneratedMessage
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PaymentService(
    private val messageBrokerProducer: MessageBrokerProducer
) {

    fun generatePayment(message: GeneratePaymentMessage) {
        val id: UUID = UUID.randomUUID()

        messageBrokerProducer.producePaymentGenerated(
            PaymentGeneratedMessage(
                paymentId = id,
                orderId = message.orderId,
                userId = message.userId,
                value = message.totalAmount,
            )
        )

        messageBrokerProducer.producePaymentConfirmed(
            PaymentConfirmedMessage(
                paymentId = id,
                orderId = message.orderId,
                userId = message.userId,
                value = message.totalAmount
            )
        )
    }
}