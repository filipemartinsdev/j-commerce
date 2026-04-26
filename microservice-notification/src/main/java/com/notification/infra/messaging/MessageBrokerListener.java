package com.notification.infra.messaging;

import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import com.notification.application.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageBrokerListener {
    private final UserNotificationService userNotificationService;

    public MessageBrokerListener(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @RabbitListener(
            queues = "${broker.queues.notifyPaymentGenerated.name}"
    )
    public void listenNotifyPaymentGenerated(NotifyPaymentGeneratedMessage message) {
        log.info("Received Notify Payment Generated message");
        userNotificationService.notifyPaymentGenerated(message);
    }

    @RabbitListener(
            queues = "${broker.queues.notifyPaymentConfirmed.name}"
    )
    public void listenNotifyConfirmed(NotifyPaymentConfirmedMessage message) {
        log.info("Received Notify Payment Confirmed message");
        userNotificationService.notifyPaymentConfirmed(message);
    }
}
