package com.orders.application.service;

import com.orders.application.message.CreateShippingMessage;
import com.orders.application.message.GeneratePaymentMessage;
import com.orders.application.message.NotifyShippingDispatchedMessage;
import com.orders.application.message.SalesOrderCancelledMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MessageBrokerProducer {
    @Value("${broker.queues.generatePayment.name}")
    private String GENERATE_PAYMENT_QUEUE_NAME;

    @Value("${broker.queues.createShipping.name}")
    private String CREATE_SHIPPING_QUEUE_NAME;

    @Value("${broker.queues.notifyShippingDispatched.name}")
    private String NOTIFY_SHIPPING_DISPATCHED_QUEUE_NAME;

    @Value("${broker.exchanges.orderCancelledFanout.name}")
    private String ORDER_CANCELLED_FANOUT_NAME;

    private final RabbitTemplate rabbitTemplate;

    public MessageBrokerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceCreateShipping(CreateShippingMessage message){
        rabbitTemplate.convertAndSend("", CREATE_SHIPPING_QUEUE_NAME, message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }

    public void produceGeneratePayment(GeneratePaymentMessage message){
        rabbitTemplate.convertAndSend("", GENERATE_PAYMENT_QUEUE_NAME, message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }

    public void produceOrderCancelled(SalesOrderCancelledMessage message) {
        rabbitTemplate.convertAndSend(ORDER_CANCELLED_FANOUT_NAME, "", message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }

    public void produceNotifyShippingDispatched(NotifyShippingDispatchedMessage message) {
        rabbitTemplate.convertAndSend("", NOTIFY_SHIPPING_DISPATCHED_QUEUE_NAME, message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }
}
