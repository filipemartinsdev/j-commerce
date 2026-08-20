package com.products.infra.messaging;

import com.products.application.message.OrderCheckedMessage;
import com.products.application.message.SKUCreatedMessage;
import com.products.application.message.SKUDeletedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageBrokerProducer {
    @Value("${broker.exchanges.orderTopic.name}")
    private String ORDER_EXCHANGE_NAME;

    @Value("${broker.exchanges.productTopic.name}")
    private String PRODUCT_EXCHANGE_NAME;

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

    public void produceSKUCreated(SKUCreatedMessage message) {
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE_NAME, "product.sku.created", message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }

    public void produceSKUDeleted(SKUDeletedMessage message) {
        rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE_NAME, "product.sku.deleted", message, msg -> {
            msg.getMessageProperties().setContentEncoding(null);
            return msg;
        });
    }
}
