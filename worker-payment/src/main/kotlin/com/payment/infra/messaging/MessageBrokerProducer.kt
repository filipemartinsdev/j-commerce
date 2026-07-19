package com.payment.infra.messaging

import com.payment.application.message.PaymentConfirmedMessage
import com.payment.application.message.PaymentGeneratedMessage
import com.payment.application.message.PaymentRefundedMessage
import com.payment.application.message.PaymentTimeoutMessage
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter

@ApplicationScoped
class MessageBrokerProducer (
    @param:Channel("payment-generated") private val paymentGeneratedEmitter: Emitter<PaymentGeneratedMessage>,
    @param:Channel("payment-confirmed") private val paymentConfirmedEmitter: Emitter<PaymentConfirmedMessage>,
    @param:Channel("payment-timeout") private val paymentTimeoutEmitter: Emitter<PaymentTimeoutMessage>,
    @param:Channel("payment-refunded") private val paymentRefundedEmitter: Emitter<PaymentRefundedMessage>,
){

    fun producePaymentGenerated(message: PaymentGeneratedMessage) {
        paymentGeneratedEmitter.send(message)
    }

    fun producePaymentConfirmed(message: PaymentConfirmedMessage) {
        paymentConfirmedEmitter.send(message)
    }

    fun producePaymentTimeout(message: PaymentTimeoutMessage) {
        paymentTimeoutEmitter.send(message)
    }

    fun producePaymentRefunded(message: PaymentRefundedMessage) {
        paymentRefundedEmitter.send(message)
    }
}