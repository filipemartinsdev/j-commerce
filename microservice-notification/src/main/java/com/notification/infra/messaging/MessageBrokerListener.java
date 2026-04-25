package com.notification.infra.messaging;

import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageBrokerListener {

    @RabbitListener(
            queues = "${broker.queues.notifyPaymentGenerated.name}"
    )
    public void listenNotifyPaymentGenerated(NotifyPaymentGeneratedMessage message) {
        // TODO
        log.info("Received Notify Payment Generated message");
    }

    @RabbitListener(
            queues = "${broker.queues.notifyPaymentConfirmed.name}"
    )
    public void listenNotifyConfirmed(NotifyPaymentConfirmedMessage message) {
        // TODO
        log.info("Received Notify Payment Confirmed message");
    }
}
