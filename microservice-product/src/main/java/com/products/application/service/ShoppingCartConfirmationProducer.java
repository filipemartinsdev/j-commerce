package com.products.application.service;

import com.products.application.dto.ShoppingCartConfirmation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShoppingCartConfirmationProducer {
    @Value("${broker.queues.shoppingCartConfirmation.name}")
    private String queueName;

    private final RabbitTemplate rabbitTemplate;

    public ShoppingCartConfirmationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produce(ShoppingCartConfirmation shoppingCartConfirmation) {
        rabbitTemplate.convertAndSend("", queueName, shoppingCartConfirmation);
    }
}
