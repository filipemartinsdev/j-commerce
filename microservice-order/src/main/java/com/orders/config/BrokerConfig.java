package com.orders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class BrokerConfig {
    @Value("${broker.queues.createOrder.name}")
    private String CREATE_ORDER_QUEUE_NAME;

    @Value("${broker.queues.generatePayment.name}")
    private String GENERATE_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.handlePaymentTimeout.name}")
    private String HANDLE_PAYMENT_TIMEOUT_QUEUE_NAME;

    @Value("${broker.queues.confirmOrderPayment.name}")
    private String CONFIRM_ORDER_PAYMENT_QUEUE_NAME;

    @Bean
    public Queue createOrderQueue() {
        return new Queue(CREATE_ORDER_QUEUE_NAME, true);
    }

    @Bean
    public Queue generatePaymentQueue() {
        return new Queue(GENERATE_PAYMENT_QUEUE_NAME, true);
    }

    @Bean
    public Queue handlePaymentTimeoutQueue() {
        return new Queue(HANDLE_PAYMENT_TIMEOUT_QUEUE_NAME, true);
    }

    @Bean
    public Queue confirmOrderPaymentQueue() {
        return new Queue(CONFIRM_ORDER_PAYMENT_QUEUE_NAME, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }
}
