package com.products.application.service;

import com.products.application.message.CreateOrderMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageBrokerProducerTests {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MessageBrokerProducer messageBrokerProducer;

    @Test
    @DisplayName("Should produce message successfully")
    void produceTestCase1() {
        ReflectionTestUtils.setField(messageBrokerProducer, "CREATE_ORDER_QUEUE_NAME", "test-queue");

        CreateOrderMessage.OrderItem item = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(),
                "Test Product",
                2,
                BigDecimal.valueOf(100)
        );
        CreateOrderMessage message = new CreateOrderMessage(
                UUID.randomUUID(),
                List.of(item),
                UUID.randomUUID()
        );

        messageBrokerProducer.produce(message);

        verify(rabbitTemplate).convertAndSend(eq(""), eq("test-queue"), eq(message), any(MessagePostProcessor.class));
    }

    @Test
    @DisplayName("Should produce message with empty items list")
    void produceTestCase2() {
        ReflectionTestUtils.setField(messageBrokerProducer, "CREATE_ORDER_QUEUE_NAME", "test-queue");

        CreateOrderMessage message = new CreateOrderMessage(
                UUID.randomUUID(),
                List.of(),
                UUID.randomUUID()
        );

        messageBrokerProducer.produce(message);

        verify(rabbitTemplate).convertAndSend(eq(""), eq("test-queue"), eq(message), any(MessagePostProcessor.class));
    }

    @Test
    @DisplayName("Should produce message with multiple items")
    void produceTestCase3() {
        ReflectionTestUtils.setField(messageBrokerProducer, "CREATE_ORDER_QUEUE_NAME", "order-queue");

        CreateOrderMessage.OrderItem item1 = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(), "Product 1", 2, BigDecimal.valueOf(50)
        );
        CreateOrderMessage.OrderItem item2 = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(), "Product 2", 1, BigDecimal.valueOf(75)
        );
        CreateOrderMessage message = new CreateOrderMessage(
                UUID.randomUUID(),
                List.of(item1, item2),
                UUID.randomUUID()
        );

        messageBrokerProducer.produce(message);

        verify(rabbitTemplate).convertAndSend(eq(""), eq("order-queue"), eq(message), any(MessagePostProcessor.class));
    }

    @Test
    @DisplayName("Should produce message with single item and high value")
    void produceTestCase4() {
        ReflectionTestUtils.setField(messageBrokerProducer, "CREATE_ORDER_QUEUE_NAME", "high-value-queue");

        CreateOrderMessage.OrderItem item = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(),
                "Expensive Product",
                1,
                BigDecimal.valueOf(9999.99)
        );
        CreateOrderMessage message = new CreateOrderMessage(
                UUID.randomUUID(),
                List.of(item),
                UUID.randomUUID()
        );

        messageBrokerProducer.produce(message);

        verify(rabbitTemplate).convertAndSend(eq(""), eq("high-value-queue"), eq(message), any(MessagePostProcessor.class));
    }
}