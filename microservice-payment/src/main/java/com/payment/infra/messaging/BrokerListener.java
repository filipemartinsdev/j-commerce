package com.payment.infra.messaging;

import com.payment.application.message.GeneratePaymentMessage;
import com.payment.application.message.WaitPendingPaymentMessage;
import com.payment.application.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("!test")
public class BrokerListener {
    private final PaymentService paymentService;

    public BrokerListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(
            queues = "${broker.queues.generatePayment.name}"
    )
    public void listenGeneratePayment(@Payload GeneratePaymentMessage message){
        log.info("Received Generate Payment message");
        paymentService.generatePayment(message);
    }
}
