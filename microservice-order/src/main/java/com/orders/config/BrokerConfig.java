package com.orders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerConfig {
    @Value("${broker.queues.shoppingCartConfirmation.name}")
    private String SHOPPING_CART_CONFIRMATION_QUEUE_NAME;

    @Bean
    public Queue shoppingCartConfirmationQueue() {
        return new Queue(SHOPPING_CART_CONFIRMATION_QUEUE_NAME, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }
}
