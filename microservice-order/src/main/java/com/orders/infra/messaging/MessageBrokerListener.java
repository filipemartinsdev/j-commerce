package com.orders.infra.messaging;

import com.orders.application.message.*;
import com.orders.application.service.AdminShippingService;
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
    private final AdminShippingService adminShippingService;

    public MessageBrokerListener(SalesOrderService salesOrderService, AdminShippingService adminShippingService) {
        this.salesOrderService = salesOrderService;
        this.adminShippingService = adminShippingService;
    }

    @RabbitListener(
            queues = "${broker.queues.createOrder.name}"
    )
    public void listenCreateOrder(@Payload CreateOrderMessage message){
        salesOrderService.createOrder(message);
    }

    @RabbitListener(
            queues = "${broker.queues.createShipping.name}"
    )
    public void listenCreateShipping(@Payload CreateShippingMessage message){
        adminShippingService.createShipping(message);
    }

    @RabbitListener(
            queues = "${broker.queues.cancelShipments.name}"
    )
    public void listenCancelShipments(@Payload SalesOrderCancelledMessage message){
        adminShippingService.cancelShipments(message.salesOrderId());
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
