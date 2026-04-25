package com.products.application.service;

import com.products.application.message.CreateOrderMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageBrokerProducer {
    @Value("${broker.queues.createOrder.name}")
    private String CREATE_ORDER_QUEUE_NAME;

    private final RabbitTemplate rabbitTemplate;

    public MessageBrokerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produce(CreateOrderMessage message) {
        rabbitTemplate.convertAndSend("", CREATE_ORDER_QUEUE_NAME, message);
    }
}
