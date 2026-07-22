package com.notification.infra.messaging;

import com.notification.application.message.*;
import com.notification.application.service.UserNotificationService;
import com.notification.domain.entity.UserNotificationCategory;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MessageBrokerConsumer {
    private static final Logger log = LoggerFactory.getLogger(MessageBrokerConsumer.class);

    private final UserNotificationService userNotificationService;

    public MessageBrokerConsumer(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @Incoming("payment-generated")
    @Transactional
    public void consumeNotifyPaymentGenerated(JsonObject payload) {
        NotifyPaymentGeneratedMessage message = payload.mapTo(NotifyPaymentGeneratedMessage.class);

        userNotificationService.create(
                message.userId(),
                "Payment generated",
                "A payment of R$" + message.amount() + " has been generated and sent by email",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("payment-confirmed")
    @Transactional
    public void consumeNotifyPaymentConfirmed(JsonObject payload) {
        NotifyPaymentConfirmedMessage message = payload.mapTo(NotifyPaymentConfirmedMessage.class);

        userNotificationService.create(
                message.userId(),
                "Payment confirmed",
                "Your payment of R$" + message.amount() + " has been confirmed",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("payment-timeout")
    @Transactional
    public void consumeNotifyPaymentTimeout(JsonObject payload) {
        NotifyPaymentTimeoutMessage message = payload.mapTo(NotifyPaymentTimeoutMessage.class);

        userNotificationService.create(
                message.userId(),
                "Payment expired",
                "Your payment of R$" + message.amount() + " has been expired and your order will be refunded.",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("order-canceled")
    @Transactional
    public void consumeNotifyCanceledOrder(JsonObject payload) {
        NotifyOrderCanceledMessage message = payload.mapTo(NotifyOrderCanceledMessage.class);

        userNotificationService.create(
                message.userId(),
                "Order canceled",
                "Your order of R$" + message.totalAmount() + " has been cancelled",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("payment-refunded")
    @Transactional
    public void consumeNotifyRefundedPayment(JsonObject payload) {
        NotifyPaymentRefundedMessage message = payload.mapTo(NotifyPaymentRefundedMessage.class);

        userNotificationService.create(
                message.userId(),
                "Payment refunded",
                "Your payment of R$" + message.amount() + " has been refunded",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("shipping-dispatched")
    @Transactional
    public void consumeNotifyShippingDispatched(JsonObject payload){
        NotifyShippingDispatchedMessage message = payload.mapTo(NotifyShippingDispatchedMessage.class);

        userNotificationService.create(
                message.userId(),
                "Shipping dispatched",
                "Your order of R$" + message.totalAmount() + " has been dispatched for delivery",
                UserNotificationCategory.Value.WARNING.id
        );
    }
}
