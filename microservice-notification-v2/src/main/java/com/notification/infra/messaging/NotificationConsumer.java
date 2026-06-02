package com.notification.infra.messaging;

import com.notification.application.message.NotifyOrderCancelledMessage;
import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @Incoming("payment-generated")
    public void consumeNotifyPaymentGenerated(NotifyPaymentGeneratedMessage message) {
        log.info("Received Notify Payment Generated Message: {}", message);
    }

    @Incoming("payment-confirmed")
    public void consumeNotifyPaymentConfirmed(NotifyPaymentConfirmedMessage message) {
        log.info("Received Notify Payment Confirmed Message: {}", message);
    }

    @Incoming("order-cancelled")
    public void consumeNotifyCancelledOrder(NotifyOrderCancelledMessage message) {
        log.info("Received Notify Order Cancelled Message: {}", message);
    }
}
