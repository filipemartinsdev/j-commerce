package com.products.infra.messaging;

import com.products.application.message.OrderCheckedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageBrokerProducer {
    @Value("${broker.exchanges.orderTopic.name}")
    private String ORDER_EXCHANGE_NAME;

    private final RabbitTemplate rabbitTemplate;

    public MessageBrokerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /*
    * Content Encoding should be null because Spring set UTF-8 as default.
    * Other applications non-spring, like Quarkus, can have trouble reading it.
    * **/
    public void produceOrderChecked(OrderCheckedMessage message){
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE_NAME, "order.checked", message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }
}
