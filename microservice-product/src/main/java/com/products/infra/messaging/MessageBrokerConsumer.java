package com.products.infra.messaging;

import com.products.application.message.OrderCheckedMessage;
import com.products.application.message.PriceUpdatedMessage;
import com.products.application.message.RefundItemsMessage;
import com.products.application.service.MessagingHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component @Profile("!test")
public class MessageBrokerConsumer {

    private final MessagingHandler messagingHandler;

    public MessageBrokerConsumer(MessagingHandler messagingHandler) {
        this.messagingHandler = messagingHandler;
    }

    @RabbitListener(queues = "${broker.queues.refundProduct.name}")
    public void listenRefundItems(@Payload RefundItemsMessage message) {
        messagingHandler.refundItems(message);
    }

    @RabbitListener(queues = "${broker.queues.decreaseStock.name}")
    public void listenDecreaseStock(@Payload OrderCheckedMessage message) {
        messagingHandler.decreaseStock(message);
    }

    @RabbitListener(queues = "${broker.queues.updatePrice.name}")
    public void listenUpdatePrice(@Payload PriceUpdatedMessage message) {
        messagingHandler.updatePrice(message);
    }
}
