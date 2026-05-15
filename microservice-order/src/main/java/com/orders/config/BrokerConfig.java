package com.orders.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class BrokerConfig {
    @Value("${broker.exchanges.orderCancelledFanout.name}")
    private String ORDER_CANCELLED_EXCHANGE_NAME;

    @Value("${broker.queues.createOrder.name}")
    private String CREATE_ORDER_QUEUE_NAME;

    @Value("${broker.queues.createShipping.name}")
    private String CREATE_SHIPPING_QUEUE_NAME;

    @Value("${broker.queues.cancelShipments.name}")
    private String CANCEL_SHIPMENTS_QUEUE_NAME;

    @Value("${broker.queues.generatePayment.name}")
    private String GENERATE_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.handlePaymentTimeout.name}")
    private String HANDLE_PAYMENT_TIMEOUT_QUEUE_NAME;

    @Value("${broker.queues.confirmOrderPayment.name}")
    private String CONFIRM_ORDER_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.refundItems.name}")
    private String REFUND_ITEMS_QUEUE_NAME;

    @Value("${broker.queues.notifyCancelledOrder.name}")
    private String NOTIFY_CANCELLED_ORDER_QUEUE_NAME;


    @Bean
    public Queue createOrderQueue() {
        return new Queue(CREATE_ORDER_QUEUE_NAME, true);
    }

    @Bean
    public Queue createShippingQueue() {
        return new Queue(CREATE_SHIPPING_QUEUE_NAME, true);
    }

    @Bean
    public Queue cancelShipmentsQueue() {
        return new Queue(CANCEL_SHIPMENTS_QUEUE_NAME, true);
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
    public FanoutExchange orderCancelledFanoutExchange() {
        return new FanoutExchange(ORDER_CANCELLED_EXCHANGE_NAME);
    }

    @Bean
    public Queue refundItemsQueue() {
        return new Queue(REFUND_ITEMS_QUEUE_NAME, true);
    }

    @Bean
    public Queue notifyCancelledOrderQueue() {
        return new Queue(NOTIFY_CANCELLED_ORDER_QUEUE_NAME, true);
    }

    @Bean
    public Binding bindingOrderCancelled1(Queue refundItemsQueue, FanoutExchange orderCancelledFanoutExchange) {
        return BindingBuilder.bind(refundItemsQueue).to(orderCancelledFanoutExchange);
    }

    @Bean
    public Binding bindingOrderCancelled2(Queue notifyCancelledOrderQueue, FanoutExchange orderCancelledFanoutExchange) {
        return BindingBuilder.bind(notifyCancelledOrderQueue).to(orderCancelledFanoutExchange);
    }

    @Bean
    public Binding bindingOrderCancelled3(Queue cancelShipmentsQueue, FanoutExchange orderCancelledFanoutExchange) {
        return BindingBuilder.bind(cancelShipmentsQueue).to(orderCancelledFanoutExchange);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
