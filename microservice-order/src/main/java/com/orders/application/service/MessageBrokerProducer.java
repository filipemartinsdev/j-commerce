package com.orders.application.service;

import com.orders.application.message.GeneratePaymentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageBrokerProducer {
    @Value("${broker.queues.generatePayment.name}")
    private String GENERATE_PAYMENT_QUEUE_NAME;

    private final RabbitTemplate rabbitTemplate;

    public MessageBrokerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceGeneratePayment(GeneratePaymentMessage message){
        log.info("producing Generate Payment Message");
        rabbitTemplate.convertAndSend("", GENERATE_PAYMENT_QUEUE_NAME, message);
    }
}
