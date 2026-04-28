package com.products.infra.messaging;

import com.products.application.message.RefundItemsMessage;
import com.products.application.service.AdminProductStockService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component @Profile("!test")
public class MessageBrokerConsumer {
    private final AdminProductStockService adminProductStockService;

    public MessageBrokerConsumer(AdminProductStockService adminProductStockService) {
        this.adminProductStockService = adminProductStockService;
    }

    @RabbitListener(queues = "${broker.queues.refundItems.name}")
    public void listenRefundItems(@Payload RefundItemsMessage message) {
        adminProductStockService.refundItems(message);
    }
}
