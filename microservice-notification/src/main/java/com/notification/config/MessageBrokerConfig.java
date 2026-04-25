package com.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class MessageBrokerConfig {
    @Value("${broker.queues.notifyPaymentGenerated.name}")
    private String NOTIFY_PAYMENT_GENERATED_QUEUE_NAME;

    @Value("${broker.queues.notifyPaymentConfirmed.name}")
    private String NOTIFY_PAYMENT_CONFIRMED_QUEUE_NAME;


    @Bean
    public Queue notifyPaymentGeneratedQueue() {
        return new Queue(NOTIFY_PAYMENT_GENERATED_QUEUE_NAME, true);
    }

    @Bean
    public Queue notifyPaymentConfirmedQueue() {
        return new Queue(NOTIFY_PAYMENT_CONFIRMED_QUEUE_NAME, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter(new ObjectMapper());
    }
}
