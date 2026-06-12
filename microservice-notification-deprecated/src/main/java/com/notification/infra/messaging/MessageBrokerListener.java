package com.notification.infra.messaging;

import com.notification.application.message.NotifyOrderCancelledMessage;
import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import com.notification.application.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component @Profile("!test")
public class MessageBrokerListener {
    private final UserNotificationService userNotificationService;

    public MessageBrokerListener(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @RabbitListener(
            queues = "${broker.queues.notifyPaymentGenerated.name}"
    )
    public void listenNotifyPaymentGenerated(NotifyPaymentGeneratedMessage message) {
        userNotificationService.notifyPaymentGenerated(message);
    }

    @RabbitListener(
            queues = "${broker.queues.notifyPaymentConfirmed.name}"
    )
    public void listenNotifyConfirmed(NotifyPaymentConfirmedMessage message) {
        userNotificationService.notifyPaymentConfirmed(message);
    }

    @RabbitListener(
            queues = "${broker.queues.notifyCancelledOrder.name}"
    )
    public void listenNotifyOrderCancelled(NotifyOrderCancelledMessage message) {
        userNotificationService.notifyCancelledOrder(message);
    }
}
