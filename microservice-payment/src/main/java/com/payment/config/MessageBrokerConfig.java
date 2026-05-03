package com.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration @Profile("!test")
public class MessageBrokerConfig {
    @Value("${broker.exchanges.paymentGeneratedFanout.name}")
    private String PAYMENT_GENERATED_EXCHANGE_NAME;

    @Value("${broker.exchanges.paymentConfirmedFanout.name}")
    private String PAYMENT_CONFIRMED_EXCHANGE_NAME;

    @Value("${broker.queues.generatePayment.name}")
    private String GENERATE_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.waitPendingPayment.name}")
    private String WAIT_PENDING_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.notifyPaymentGenerated.name}")
    private String NOTIFY_PAYMENT_GENERATED_QUEUE_NAME;

    @Value("${broker.queues.confirmOrderPayment.name}")
    private String CONFIRM_ORDER_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.notifyPaymentConfirmed.name}")
    private String NOTIFY_PAYMENT_CONFIRMED_QUEUE_NAME;

    @Value("${broker.queues.handlePaymentTimeout.name}")
    private String HANDLE_PAYMENT_TIMEOUT_QUEUE_NAME;

    @Bean
    public FanoutExchange paymentGeneratedExchange() {
        return new FanoutExchange(PAYMENT_GENERATED_EXCHANGE_NAME);
    }

    @Bean
    public FanoutExchange paymentConfirmedExchange() {
        return new FanoutExchange(PAYMENT_CONFIRMED_EXCHANGE_NAME);
    }

    @Bean
    public Queue generatePaymentQueue() {
        return new Queue(GENERATE_PAYMENT_QUEUE_NAME, true);
    }

    @Bean
    public Queue waitPendingPaymentQueue() {
        return QueueBuilder.durable(WAIT_PENDING_PAYMENT_QUEUE_NAME)
                .withArgument("x-message-ttl", 86400) // 1 day
                .withArgument("x-dead-letter-routing-key", HANDLE_PAYMENT_TIMEOUT_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .build();
    }

    @Bean
    public Queue notifyPaymentGeneratedQueue() {
        return new Queue(NOTIFY_PAYMENT_GENERATED_QUEUE_NAME, true);
    }

    @Bean
    public Queue notifyPaymentConfirmedQueue() {
        return new Queue(NOTIFY_PAYMENT_CONFIRMED_QUEUE_NAME, true);
    }

    @Bean
    public Queue confirmOrderPaymentQueue() {
        return new Queue(CONFIRM_ORDER_PAYMENT_QUEUE_NAME, true);
    }

    @Bean
    public Binding bindingPaymentGenerated1(Queue waitPendingPaymentQueue, FanoutExchange paymentGeneratedExchange) {
        return BindingBuilder.bind(waitPendingPaymentQueue).to(paymentGeneratedExchange);
    }

    @Bean
    public Binding bindingPaymentGenerated2(Queue notifyPaymentGeneratedQueue, FanoutExchange paymentGeneratedExchange) {
        return BindingBuilder.bind(notifyPaymentGeneratedQueue).to(paymentGeneratedExchange);
    }

    @Bean
    public Binding bindingPaymentConfirmed1(Queue confirmOrderPaymentQueue, FanoutExchange paymentConfirmedExchange) {
        return BindingBuilder.bind(confirmOrderPaymentQueue).to(paymentConfirmedExchange);
    }

    @Bean
    public Binding bindingPaymentConfirmed2(Queue notifyPaymentConfirmedQueue, FanoutExchange paymentConfirmedExchange) {
        return BindingBuilder.bind(notifyPaymentConfirmedQueue).to(paymentConfirmedExchange);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
