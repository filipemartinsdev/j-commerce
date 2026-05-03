package com.payment.application.service;

import com.payment.application.message.GeneratePaymentMessage;
import com.payment.application.message.PaymentConfirmedMessage;
import com.payment.application.message.PaymentGeneratedMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTests {

    @Mock
    private MessageBrokerProducer messageBrokerProducer;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should generate payment and send messages")
    void generatePaymentTestCase1() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("100.00");

        GeneratePaymentMessage message = new GeneratePaymentMessage(orderId, userId, totalAmount);

        doNothing().when(messageBrokerProducer).producePaymentGeneratedMessage(any(PaymentGeneratedMessage.class));
        doNothing().when(messageBrokerProducer).producePaymentConfirmedMessage(any(PaymentConfirmedMessage.class));

        paymentService.generatePayment(message);

        verify(messageBrokerProducer).producePaymentGeneratedMessage(any(PaymentGeneratedMessage.class));
        verify(messageBrokerProducer).producePaymentConfirmedMessage(any(PaymentConfirmedMessage.class));
    }

    @Test
    @DisplayName("Should call message broker producer with correct parameters")
    void generatePaymentTestCase2() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("250.00");

        GeneratePaymentMessage message = new GeneratePaymentMessage(orderId, userId, totalAmount);

        paymentService.generatePayment(message);

        verify(messageBrokerProducer).producePaymentGeneratedMessage(any(PaymentGeneratedMessage.class));
        verify(messageBrokerProducer).producePaymentConfirmedMessage(any(PaymentConfirmedMessage.class));
    }
}