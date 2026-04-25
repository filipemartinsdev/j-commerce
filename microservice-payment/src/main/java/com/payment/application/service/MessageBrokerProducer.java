package com.payment.application.service;

import com.payment.application.message.PaymentConfirmedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageBrokerProducer {
    @Value("${broker.exchanges.paymentGeneratedFanout.name}")
    private String PAYMENT_GENERATED_EXCHANGE_NAME;

    @Value("${broker.exchanges.paymentConfirmedFanout.name}")
    private String PAYMENT_CONFIRMED_EXCHANGE_NAME;

    private final RabbitTemplate rabbitTemplate;

    public MessageBrokerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void producePaymentGeneratedMessage(PaymentGeneratedMessage message) {
        rabbitTemplate.convertAndSend(PAYMENT_GENERATED_EXCHANGE_NAME, "", message);
    }

    public void producePaymentConfirmedMessage(PaymentConfirmedMessage message) {
        rabbitTemplate.convertAndSend(PAYMENT_CONFIRMED_EXCHANGE_NAME, "", message);
    }
}
