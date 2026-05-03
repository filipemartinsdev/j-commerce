package com.orders.application.service;

import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.SalesOrderResponse;
import com.orders.application.dto.SalesOrderSummaryResponse;
import com.orders.application.exception.CantCancelSalesOrderException;
import com.orders.application.exception.SalesOrderNotFoundException;
import com.orders.application.service.mapper.SalesOrderMapper;
import com.orders.domain.entity.SalesOrder;
import com.orders.domain.entity.SalesOrderStatus;
import com.orders.domain.entity.Shipping;
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

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderItemRepository salesOrderItemRepository;

    @Mock
    private SalesOrderStatusRepository salesOrderStatusRepository;

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private DeliveryAddressRepository deliveryAddressRepository;

    @Mock
    private ShippingStatusRepository shippingStatusRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private MessageBrokerProducer messageBrokerProducer;

    @InjectMocks
    private SalesOrderService salesOrderService;

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
                new BigDecimal("100.00"),
                "PENDING"
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
        order.setShipping(shipping);
        order.setItems(List.of());

        SalesOrderSummaryResponse response = new SalesOrderSummaryResponse(
                orderId,
                "PENDING",
                "PENDING",
                new BigDecimal("100.00"),
                List.of(),
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
        status.setId(1);
        status.setName("PENDING");

        Shipping shipping = new Shipping();

        SalesOrder order = new SalesOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(status);
        order.setShipping(shipping);

        SalesOrderStatus cancelledStatus = new SalesOrderStatus();
        cancelledStatus.setId(6);
        cancelledStatus.setName("CANCELLED");

        when(salesOrderRepository.findById(orderId))
                .thenReturn(Optional.of(order));
        when(salesOrderStatusRepository.getReferenceById(6))
                .thenReturn(cancelledStatus);
        when(salesOrderRepository.save(any(SalesOrder.class)))
                .thenReturn(order);

        salesOrderService.requestToCancelOrder(orderId, userId);

        verify(salesOrderRepository).findById(orderId);
        verify(salesOrderStatusRepository).getReferenceById(6);
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