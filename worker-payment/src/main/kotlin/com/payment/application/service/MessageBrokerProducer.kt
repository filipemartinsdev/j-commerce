package com.payment.application.service

import com.payment.application.message.PaymentConfirmedMessage
import com.payment.application.message.PaymentGeneratedMessage
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Outgoing

@ApplicationScoped
class MessageBrokerProducer (
    @param:Channel("payment-generated") private val paymentGeneratedEmitter: Emitter<PaymentGeneratedMessage>,
    @param:Channel("payment-confirmed") private val paymentConfirmedEmitter: Emitter<PaymentConfirmedMessage>,
){

    fun producePaymentGenerated(message: PaymentGeneratedMessage) {
        paymentGeneratedEmitter.send(message)
    }

    fun producePaymentConfirmed(message: PaymentConfirmedMessage) {
        paymentConfirmedEmitter.send(message)
    }
}