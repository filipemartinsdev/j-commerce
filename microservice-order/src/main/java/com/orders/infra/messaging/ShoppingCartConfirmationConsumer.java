package com.orders.infra.messaging;

import com.orders.application.dto.ShoppingCartConfirmation;
import com.orders.application.service.SalesOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ShoppingCartConfirmationConsumer {
    private final SalesOrderService salesOrderService;

    public ShoppingCartConfirmationConsumer(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @RabbitListener(
            queues = "${broker.queues.shoppingCartConfirmation.name}"
    )
    public void listen(@Payload ShoppingCartConfirmation dto){
        salesOrderService.confirmShoppingCart(dto);
    }
}
