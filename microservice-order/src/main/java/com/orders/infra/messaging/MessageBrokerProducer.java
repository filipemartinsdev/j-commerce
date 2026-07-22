package com.orders.infra.messaging;

import com.orders.application.message.SalesOrderCanceledMessage;
import com.orders.application.message.SalesOrderCreatedMessage;
import com.orders.application.message.SalesOrderDispatchedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Content Encoding should be null because Spring set UTF-8 as default.
 * Other applications non-spring, like Quarkus, can have trouble reading it.
 * **/
@Component
public class MessageBrokerProducer {
    @Value("${broker.exchanges.orderTopic.name}")
    private String ORDER_EXCHANGE_NAME;

    private final RabbitTemplate rabbitTemplate;

    public MessageBrokerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceOrderCreated(SalesOrderCreatedMessage message){
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, "order.created", message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }

    public void produceOrderCanceled(SalesOrderCanceledMessage message){
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, "order.canceled", message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }

    public void produceOrderDispatched(SalesOrderDispatchedMessage orderDispatchedMessage) {
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, "order.dispatched", orderDispatchedMessage, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }
}
