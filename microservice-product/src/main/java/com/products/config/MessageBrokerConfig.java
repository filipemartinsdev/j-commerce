package com.products.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class MessageBrokerConfig {
    @Value("${broker.queues.createOrder.name}")
    private String CREATE_ORDER_QUEUE_NAME;

    @Value("${broker.queues.refundItems.name}")
    private String REFUND_ITEMS_QUEUE_NAME;

    @Bean
    public Queue shoppingCartConfirmationQueue() {
        return new Queue(CREATE_ORDER_QUEUE_NAME, true);
    }

    @Bean Queue refundItemsQueue() {
        return new Queue(REFUND_ITEMS_QUEUE_NAME, true);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
