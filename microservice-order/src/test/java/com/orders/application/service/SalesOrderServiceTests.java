package com.orders.application.service;

import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.SalesOrderResponse;
import com.orders.application.dto.SalesOrderSummaryResponse;
import com.orders.application.exception.CantCancelSalesOrderException;
import com.orders.application.exception.CantCreateSalesOrderException;
import com.orders.application.exception.SalesOrderNotFoundException;
import com.orders.application.message.CreateOrderMessage;
import com.orders.application.message.CreateShippingMessage;
import com.orders.application.message.GeneratePaymentMessage;
import com.orders.application.service.mapper.SalesOrderMapper;
import com.orders.domain.entity.DeliveryAddress;
import com.orders.domain.entity.SalesOrder;
import com.orders.domain.entity.SalesOrderItem;
import com.orders.domain.entity.SalesOrderStatus;
import com.orders.domain.entity.Shipping;
import com.orders.domain.entity.ShippingStatus;
import com.orders.infra.persistence.DeliveryAddressRepository;
import com.orders.infra.persistence.SalesOrderItemRepository;
import com.orders.infra.persistence.SalesOrderRepository;
import com.orders.infra.persistence.SalesOrderStatusRepository;
import com.orders.infra.persistence.ShippingRepository;
import com.orders.infra.persistence.ShippingStatusRepository;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalesOrderServiceTests {
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private SalesOrderStatusRepository salesOrderStatusRepository;
    @Mock private SalesOrderMapper salesOrderMapper;
    @Mock private MessageBrokerProducer messageBrokerProducer;

    @InjectMocks
    private SalesOrderService salesOrderService;

    @Test
    @DisplayName("Should create order successfully")
    void createOrderTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setId(addressId);
        deliveryAddress.setLatitude(-23.0);
        deliveryAddress.setLongitude(-46.0);

        SalesOrderStatus pendingStatus = new SalesOrderStatus();
        pendingStatus.setId(SalesOrderStatus.Value.PENDING.getId());
        pendingStatus.setName("PENDING");

        ShippingStatus shippingPendingStatus = new ShippingStatus();
        shippingPendingStatus.setId(ShippingStatus.Value.PENDING.getId());
        shippingPendingStatus.setName("PENDING");

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(pendingStatus);

        SalesOrderItem item = new SalesOrderItem();
        item.setProductSkuId(UUID.randomUUID());
        item.setUnits(2);
        item.setUnitPrice(new BigDecimal("50.00"));
        item.setSalesOrder(order);
        order.setItems(List.of(item));

        CreateOrderMessage.OrderItem orderItem = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(),
                "Product",
                2,
                new BigDecimal("50.00")
        );

        CreateOrderMessage message = new CreateOrderMessage(
                userId,
                List.of(orderItem),
                addressId
        );

        when(salesOrderStatusRepository.getReferenceById(SalesOrderStatus.Value.PENDING.getId()))
                .thenReturn(pendingStatus);
        when(salesOrderRepository.save(any(SalesOrder.class)))
                .thenReturn(order);

        salesOrderService.createOrder(message);

        verify(salesOrderRepository).save(any(SalesOrder.class));
        verify(messageBrokerProducer).produceCreateShipping(any(CreateShippingMessage.class));
        verify(messageBrokerProducer).produceGeneratePayment(any(GeneratePaymentMessage.class));
    }

    @Test
    @DisplayName("Should throw CantCreateSalesOrderException if item list is empty")
    void createOrderTestCase2() {
        UUID userId = UUID.randomUUID();

        CreateOrderMessage message = new CreateOrderMessage(
                userId,
                List.of(),
                UUID.randomUUID()
        );

        assertThrows(CantCreateSalesOrderException.class, () -> {
            salesOrderService.createOrder(message);
        });

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        verify(messageBrokerProducer, never()).produceGeneratePayment(any(GeneratePaymentMessage.class));
    }

    @Test
    @DisplayName("Should return paginated sales orders")
    void getAllByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        SalesOrderStatus status = new SalesOrderStatus();
        status.setId(1);
        status.setName("PENDING");

        SalesOrder order = new SalesOrder();
        order.setId(UUID.randomUUID());
        order.setUserId(userId);
        order.setStatus(status);

        Page<SalesOrder> page = new PageImpl<>(List.of(order), pageable, 1);

        SalesOrderResponse response = new SalesOrderResponse(
                order.getId(),
                Instant.now(),
                "PENDING",
                new BigDecimal("100.00")
        );

        when(salesOrderRepository.findAllByUserId(userId, pageable))
                .thenReturn(page);
        when(salesOrderMapper.toResponse(order))
                .thenReturn(response);

        PagedResponse<SalesOrderResponse> result = salesOrderService.getAllByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("PENDING", result.content().get(0).status());
        verify(salesOrderRepository).findAllByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no orders")
    void getAllByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<SalesOrder> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(salesOrderRepository.findAllByUserId(userId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<SalesOrderResponse> result = salesOrderService.getAllByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(salesOrderRepository).findAllByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should return sales order summary by id")
    void getSummaryByIdTestCase1() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        SalesOrderStatus orderStatus = new SalesOrderStatus();
        orderStatus.setId(1);
        orderStatus.setName("PENDING");

        Shipping shipping = new Shipping();
        shipping.setId(UUID.randomUUID());

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(orderStatus);
        order.setShipments(List.of(shipping));
        order.setItems(List.of());

        SalesOrderSummaryResponse response = new SalesOrderSummaryResponse(
                orderId,
                "PENDING",
                new BigDecimal("100.00"),
                List.of(),
                null,
                null,
                Instant.now()
        );

        when(salesOrderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.of(order));
        when(salesOrderMapper.toSummaryResponse(order))
                .thenReturn(response);

        SalesOrderSummaryResponse result = salesOrderService.getSummaryById(orderId, userId);

        assertNotNull(result);
        assertEquals("PENDING", result.status());
        verify(salesOrderRepository).findByIdAndUserId(orderId, userId);
    }

    @Test
    @DisplayName("Should throw SalesOrderNotFoundException when order not found")
    void getSummaryByIdTestCase2() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(salesOrderRepository.findByIdAndUserId(orderId, userId))
                .thenReturn(Optional.empty());

        assertThrows(SalesOrderNotFoundException.class, () -> {
            salesOrderService.getSummaryById(orderId, userId);
        });

        verify(salesOrderRepository).findByIdAndUserId(orderId, userId);
    }

    @Test
    @DisplayName("Should cancel order when status is PENDING")
    void requestToCancelOrderTestCase1() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        SalesOrderStatus status = new SalesOrderStatus();
        status.setId(SalesOrderStatus.Value.PENDING.getId());
        status.setName("PENDING");

        Shipping shipping = new Shipping();

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(status);
        order.setShipments(List.of(shipping));

        SalesOrderStatus cancelledStatus = new SalesOrderStatus();
        cancelledStatus.setId(SalesOrderStatus.Value.CANCELLED.getId());
        cancelledStatus.setName("CANCELLED");

        when(salesOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(salesOrderStatusRepository.getReferenceById(SalesOrderStatus.Value.CANCELLED.getId()))
                .thenReturn(cancelledStatus);
        when(salesOrderRepository.save(any(SalesOrder.class)))
                .thenReturn(order);

        salesOrderService.requestToCancelOrder(orderId, userId);

        verify(salesOrderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw SalesOrderNotFoundException when order not found for cancellation")
    void requestToCancelOrderTestCase2() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(salesOrderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(SalesOrderNotFoundException.class, () -> {
            salesOrderService.requestToCancelOrder(orderId, userId);
        });

        verify(salesOrderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw CantCancelSalesOrderException when order cannot be cancelled")
    void requestToCancelOrderTestCase3() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        SalesOrderStatus status = new SalesOrderStatus();
        status.setId(5);
        status.setName("DELIVERED");

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(status);

        when(salesOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(CantCancelSalesOrderException.class, () -> {
            salesOrderService.requestToCancelOrder(orderId, userId);
        });

        verify(salesOrderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw CantCancelSalesOrderException when user is not owner")
    void requestToCancelOrderTestCase4() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        SalesOrderStatus status = new SalesOrderStatus();
        status.setId(1);
        status.setName("PENDING");

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setUserId(differentUserId);
        order.setStatus(status);

        when(salesOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        assertThrows(CantCancelSalesOrderException.class, () -> {
            salesOrderService.requestToCancelOrder(orderId, userId);
        });

        verify(salesOrderRepository).findById(orderId);
    }
}