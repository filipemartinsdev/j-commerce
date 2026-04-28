package com.orders.infra.messaging;

import com.orders.application.message.ConfirmOrderMessage;
import com.orders.application.message.CreateOrderMessage;
import com.orders.application.message.HandlePaymentTimeoutMessage;
import com.orders.application.message.PaymentConfirmedMessage;
import com.orders.application.service.SalesOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
@Slf4j
public class MessageBrokerListener {
    private final SalesOrderService salesOrderService;

    public MessageBrokerListener(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @RabbitListener(
            queues = "${broker.queues.createOrder.name}"
    )
    public void listenCreateOrder(@Payload CreateOrderMessage message){
        salesOrderService.createOrder(message);
    }

    @RabbitListener(
            queues = "${broker.queues.handlePaymentTimeout.name}"
    )
    public void listenHandlePaymentTimeout(@Payload HandlePaymentTimeoutMessage message){
        salesOrderService.handleSalesOrderPaymentTimeout(message);
    }

    @RabbitListener(
            queues = "${broker.queues.confirmOrderPayment.name}"
    )
    public void listenConfirmOrder(@Payload PaymentConfirmedMessage message){
        salesOrderService.confirmOrderPayment(message);
    }
}
