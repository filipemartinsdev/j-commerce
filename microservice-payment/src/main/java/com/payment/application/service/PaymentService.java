package com.payment.application.service;

import com.payment.application.message.GeneratePaymentMessage;
import com.payment.application.message.PaymentConfirmedMessage;
import com.payment.application.message.PaymentGeneratedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class PaymentService {

    private final MessageBrokerProducer messageBrokerProducer;

    public PaymentService(MessageBrokerProducer messageBrokerProducer) {
        this.messageBrokerProducer = messageBrokerProducer;
    }

    public void generatePayment(GeneratePaymentMessage message){
        UUID mockPaymentId = UUID.randomUUID();

        messageBrokerProducer.producePaymentGeneratedMessage(new PaymentGeneratedMessage(
                mockPaymentId, message.orderId(), message.userId(), message.totalAmount()
        ));

        messageBrokerProducer.producePaymentConfirmedMessage(new PaymentConfirmedMessage(
                mockPaymentId, message.orderId(), message.userId(), message.totalAmount()
        ));
    }
}
