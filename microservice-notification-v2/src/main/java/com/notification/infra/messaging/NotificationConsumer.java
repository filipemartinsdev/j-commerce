package com.notification.infra.messaging;

import com.notification.application.message.NotifyOrderCancelledMessage;
import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import com.notification.application.message.NotifyShippingDispatchedMessage;
import com.notification.application.service.UserNotificationService;
import com.notification.domain.entity.UserNotificationCategory;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final UserNotificationService userNotificationService;

    public NotificationConsumer(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @Incoming("payment-generated")
    @Transactional
    public void consumeNotifyPaymentGenerated(JsonObject payload) {
        NotifyPaymentGeneratedMessage message = payload.mapTo(NotifyPaymentGeneratedMessage.class);

        log.info("Received Notify Payment Generated Message: {}", message);

        userNotificationService.create(
                message.userId(),
                "Payment generated",
                "A payment of R$" + message.value() + " has been generated and sent by email",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("payment-confirmed")
    @Transactional
    public void consumeNotifyPaymentConfirmed(JsonObject payload) {
        NotifyPaymentConfirmedMessage message = payload.mapTo(NotifyPaymentConfirmedMessage.class);

        log.info("Received Notify Payment Confirmed Message: {}", message);

        userNotificationService.create(
                message.userId(),
                "Payment confirmed",
                "Your payment of R$" + message.value() + " has been confirmed",
                UserNotificationCategory.Value.WARNING.id
        );
    }

    @Incoming("order-cancelled")
    @Transactional
    public void consumeNotifyCancelledOrder(JsonObject payload) {
        NotifyOrderCancelledMessage message = payload.mapTo(NotifyOrderCancelledMessage.class);

        userNotificationService.create(
                message.userId(),
                "Order cancelled",
                "Your order of R$" + message.value() + " has been cancelled",
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
                "Your order of R$" + message.orderValue() + " has been dispatched for delivery",
                UserNotificationCategory.Value.WARNING.id
        );
    }
}
