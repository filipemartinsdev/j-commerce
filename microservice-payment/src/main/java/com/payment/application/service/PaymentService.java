package com.payment.application.service;

import com.payment.application.message.GeneratePaymentMessage;
import com.payment.application.message.PaymentConfirmedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentService {

    private final MessageBrokerProducer messageBrokerProducer;

    public PaymentService(MessageBrokerProducer messageBrokerProducer) {
        this.messageBrokerProducer = messageBrokerProducer;
    }

    public void generatePayment(GeneratePaymentMessage message){
        messageBrokerProducer.producePaymentGeneratedMessage(new PaymentGeneratedMessage());
        log.info("Producing Payment Generated message");
        messageBrokerProducer.producePaymentConfirmedMessage(new PaymentConfirmedMessage());
        log.info("Producing Payment Confirmed message");
    }
}
