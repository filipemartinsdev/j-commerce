package com.orders.application.service;

import com.orders.application.dto.ShippingResponse;
import com.orders.application.exception.*;
import com.orders.application.message.SalesOrderCreatedMessage;
import com.orders.application.service.mapper.ShippingMapper;
import com.orders.domain.entity.*;
import com.orders.infra.messaging.MessageBrokerProducer;
import com.orders.infra.persistence.DeliveryAddressRepository;
import com.orders.infra.persistence.SalesOrderRepository;
import com.orders.infra.persistence.ShippingRepository;
import com.orders.infra.persistence.ShippingStatusRepository;
import io.github.responsekit.core.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminShippingServiceTests {
    @Mock private ShippingRepository shippingRepository;
    @Mock private ShippingStatusRepository shippingStatusRepository;
    @Mock private ShippingMapper shippingMapper;
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private DeliveryAddressRepository deliveryAddressRepository;
    @Mock private DeliveryDateCalculator deliveryDateCalculator;
    @Mock private MessageBrokerProducer messageBrokerProducer;

    @InjectMocks private AdminShippingService adminShippingService;

    @Test @DisplayName("Should create shipping from message successfully")
    void createShippingTestCase1() {
        UUID salesOrderId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();
        Instant deliveryDate = Instant.now().plusSeconds(86400);

        var message = new SalesOrderCreatedMessage(salesOrderId, null, deliveryAddressId, null);

        var deliveryAddress = new DeliveryAddress();
        deliveryAddress.setId(deliveryAddressId);
        deliveryAddress.setLatitude(-23.5505);
        deliveryAddress.setLongitude(-46.6333);

        var salesOrder = new SalesOrder();
        salesOrder.setId(salesOrderId);

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        when(deliveryAddressRepository.findById(deliveryAddressId))
                .thenReturn(Optional.of(deliveryAddress));
        when(salesOrderRepository.getReferenceById(salesOrderId))
                .thenReturn(salesOrder);
        when(shippingStatusRepository.getReferenceById(ShippingStatus.Value.PENDING.getId()))
                .thenReturn(shippingStatus);
        when(deliveryDateCalculator.getDeliveryDate(anyDouble(), anyDouble()))
                .thenReturn(deliveryDate);

        adminShippingService.createShipping(message);

        verify(deliveryAddressRepository).findById(deliveryAddressId);
        verify(salesOrderRepository).getReferenceById(salesOrderId);
        verify(shippingStatusRepository).getReferenceById(ShippingStatus.Value.PENDING.getId());
        verify(deliveryDateCalculator).getDeliveryDate(-23.5505, -46.6333);
        verify(shippingRepository).save(any(Shipping.class));
    }

    @Test @DisplayName("Should throw DeliveryAddressNotFoundException if DeliveryAddress not exists by ID")
    void createShippingTestCase2() {
        UUID salesOrderId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();

        var message = new SalesOrderCreatedMessage(salesOrderId, null, deliveryAddressId, null);

        when(deliveryAddressRepository.findById(deliveryAddressId))
                .thenReturn(Optional.empty());

        assertThrows(DeliveryAddressNotFoundException.class, () -> {
            adminShippingService.createShipping(message);
        });

        verify(deliveryAddressRepository).findById(deliveryAddressId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should cancel shipments successfully")
    void cancelShipmentsBySalesOrderIdTestCase1() {
        UUID salesOrderId = UUID.randomUUID();

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CANCELLED.getId());

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(salesOrderId);
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(UUID.randomUUID());
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        salesOrder.setShipments(List.of(shipping));

        when(salesOrderRepository.findById(salesOrderId))
                .thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(salesOrder))
                .thenReturn(salesOrder);
        when(shippingStatusRepository.getReferenceById(ShippingStatus.Value.CANCELLED.getId()))
                .thenReturn(shippingStatus);

        adminShippingService.cancelShipmentsBySalesOrderId(salesOrderId);

        verify(salesOrderRepository).findById(salesOrderId);
        verify(shippingStatusRepository).getReferenceById(ShippingStatus.Value.CANCELLED.getId());
        verify(salesOrderRepository).save(salesOrder);
    }

    @Test @DisplayName("Should throw SalesOrderNotFoundException if sales order not exists by ID")
    void cancelShipmentsBySalesOrderIdTestCase2() {
        UUID salesOrderId = UUID.randomUUID();

        when(salesOrderRepository.findById(salesOrderId))
                .thenReturn(Optional.empty());

        assertThrows(SalesOrderNotFoundException.class, () -> {
            adminShippingService.cancelShipmentsBySalesOrderId(salesOrderId);
        });

        verify(salesOrderRepository).findById(salesOrderId);
        verify(salesOrderRepository, never()).save(any());
    }

    @Test @DisplayName("Should cancel shipping successfully")
    void cancelShippingTestCase1() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));
        when(shippingStatusRepository.getReferenceById(ShippingStatus.Value.CANCELLED.getId()))
                .thenReturn(shippingStatus);

        adminShippingService.cancelShipping(shippingId);

        verify(shippingRepository).findById(shippingId);
        verify(shippingStatusRepository).getReferenceById(ShippingStatus.Value.CANCELLED.getId());
        verify(shippingRepository).save(shipping);
    }

    @Test @DisplayName("Should throw ShippingNotFoundException if shipping not exists by ID")
    void cancelShippingTestCase2() {
        UUID shippingId = UUID.randomUUID();

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> {
            adminShippingService.cancelShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantTransitionShippingStatusException if shipping is already cancelled")
    void cancelShippingTestCase3() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.CANCELLED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));

        assertThrows(CantTransitionShippingStatusException.class, () -> {
            adminShippingService.cancelShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantTransitionShippingStatusException if shipping is delivered")
    void cancelShippingTestCase4() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.DELIVERED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));

        assertThrows(CantTransitionShippingStatusException.class, () -> {
            adminShippingService.cancelShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should retrieve shipment successfully")
    void getAllTestCase1() {
        Pageable pageable = PageRequest.of(0, 10);

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());
        shippingStatus.setName("PENDING");

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var deliveryAddress = new DeliveryAddress();
        deliveryAddress.setId(UUID.randomUUID());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(UUID.randomUUID());
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);
        shipping.setDeliveryAddress(deliveryAddress);
        shipping.setCreatedAt(Instant.now());

        var shippingResponse = new ShippingResponse(
                shipping.getId(),
                "PENDING",
                salesOrder.getId(),
                deliveryAddress.getId(),
                Instant.now(),
                null,
                Instant.now()
        );

        Page<Shipping> page = new PageImpl<>(List.of(shipping), pageable, 1);

        when(shippingRepository.findAll(pageable)).thenReturn(page);

        var pagedResponse = PagedResponse
                .content(List.of(shippingResponse))
                .page(0).size(10).totalElements(1L).totalPages(1)
                .isLast(true)
                .build();

        when(shippingMapper.toResponse(any())).thenReturn(shippingResponse);

        PagedResponse<ShippingResponse> result = adminShippingService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements);
        verify(shippingRepository).findAll(pageable);
    }

    @Test @DisplayName("Should return empty PagedResponse if not exists any shipping")
    void getAllTestCase2() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Shipping> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        var pagedResponse = PagedResponse
                .content(List.of())
                .page(0).size(10).totalElements(0L).totalPages(0)
                .isLast(true)
                .build();

        when(shippingRepository.findAll(pageable)).thenReturn(emptyPage);

        PagedResponse<ShippingResponse> result = adminShippingService.getAll(pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements);
        assertTrue(result.content.isEmpty());
        verify(shippingRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve shipment by sales order successfully")
    void getAllBySalesOrderIdTestCase1() {
        UUID salesOrderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(salesOrderId);
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(UUID.randomUUID());
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        var shippingResponse = new ShippingResponse(
                shipping.getId(),
                "PENDING",
                salesOrderId,
                UUID.randomUUID(),
                Instant.now(),
                null,
                Instant.now()
        );

        Page<Shipping> page = new PageImpl<>(List.of(shipping), pageable, 1);

        var pagedResponse = PagedResponse
                .content(List.of(shippingResponse))
                .page(0).size(10).totalElements(1L).totalPages(1)
                .isLast(true).build();

        when(shippingRepository.findAllBySalesOrderId(salesOrderId, pageable)).thenReturn(page);

        PagedResponse<ShippingResponse> result = adminShippingService.getAllBySalesOrderId(salesOrderId, pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements);
        verify(shippingRepository).findAllBySalesOrderId(salesOrderId, pageable);
    }

    @Test @DisplayName("Should return empty PagedResponse when sales order not exists")
    void getAllBySalesOrderIdTestCase2() {
        UUID salesOrderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Shipping> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        var pagedResponse = PagedResponse
                .content(List.of())
                .page(0).size(10).totalElements(0L).totalPages(0)
                .isLast(true).build();

        when(shippingRepository.findAllBySalesOrderId(salesOrderId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<ShippingResponse> result = adminShippingService.getAllBySalesOrderId(salesOrderId, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements);
        assertTrue(result.content.isEmpty());
    }

    @Test @DisplayName("Should return empty PagedResponse if not exists any shipping by sales order")
    void getAllBySalesOrderIdTestCase3() {
        UUID salesOrderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Shipping> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        var pagedResponse = PagedResponse
                .content(List.of())
                .page(0).size(10).totalElements(0L).totalPages(0)
                .isLast(true).build();

        when(shippingRepository.findAllBySalesOrderId(salesOrderId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<ShippingResponse> result = adminShippingService.getAllBySalesOrderId(salesOrderId, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements);
        assertTrue(result.content.isEmpty());
    }

    @Test @DisplayName("Should dispatch shipping successfully")
    void dispatchShippingTestCase1() {
        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var deliveryAddress = new DeliveryAddress();
        deliveryAddress.setId(UUID.randomUUID());

        var shipping = new Shipping();
        shipping.setId(UUID.randomUUID());
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);
        shipping.setDeliveryAddress(deliveryAddress);

        var dispatchedStatus = new ShippingStatus();
        dispatchedStatus.setId(ShippingStatus.Value.DISPATCHED.getId());

        when(shippingRepository.findById(shipping.getId()))
                .thenReturn(Optional.of(shipping));
        when(shippingStatusRepository.getReferenceById(ShippingStatus.Value.DISPATCHED.getId()))
                .thenReturn(dispatchedStatus);
        doNothing().when(messageBrokerProducer).produceOrderDispatched(any());

        adminShippingService.dispatchShipping(shipping.getId());

        verify(shippingRepository).findById(shipping.getId());
        verify(shippingStatusRepository).getReferenceById(ShippingStatus.Value.DISPATCHED.getId());
        verify(shippingRepository).save(shipping);
        verify(messageBrokerProducer).produceOrderDispatched(any());
    }

    @Test @DisplayName("Should throw ShippingNotFoundException if shipping not exists by ID")
    void dispatchShippingTestCase2() {
        UUID shippingId = UUID.randomUUID();

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> {
            adminShippingService.dispatchShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantTransitionShippingStatusException if shipping is not PENDING")
    void dispatchShippingTestCase3() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.DISPATCHED.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));

        assertThrows(CantTransitionShippingStatusException.class, () -> {
            adminShippingService.dispatchShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantDispatchShippingException if sales order status is not CONFIRMED")
    void dispatchShippingTestCase4() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.PENDING.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));

        assertThrows(CantDispatchShippingException.class, () -> {
            adminShippingService.dispatchShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should start shipping successfully")
    void startShippingTestCase1() {
        UUID shippingId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.DISPATCHED.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);
        shipping.setDriverId(null);

        var inTransitStatus = new ShippingStatus();
        inTransitStatus.setId(ShippingStatus.Value.IN_TRANSIT.getId());

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));
        when(shippingStatusRepository.getReferenceById(ShippingStatus.Value.IN_TRANSIT.getId()))
                .thenReturn(inTransitStatus);

        adminShippingService.startShipping(shippingId, driverId);

        assertEquals(driverId, shipping.getDriverId());
        verify(shippingRepository).findById(shippingId);
        verify(shippingStatusRepository).getReferenceById(ShippingStatus.Value.IN_TRANSIT.getId());
        verify(shippingRepository).save(shipping);
    }

    @Test @DisplayName("Should throw ShippingNotFoundException if shipping not exists by ID")
    void startShippingTestCase2() {
        UUID shippingId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> {
            adminShippingService.startShipping(shippingId, driverId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantTransitionShippingStatusException if shipping status is not DISPATCHED")
    void startShippingTestCase3() {
        UUID shippingId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.PENDING.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);
        shipping.setDriverId(null);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));

        assertThrows(CantTransitionShippingStatusException.class, () -> {
            adminShippingService.startShipping(shippingId, driverId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should finish shipping successfully")
    void finishShippingTestCase1() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.IN_TRANSIT.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        var deliveredStatus = new ShippingStatus();
        deliveredStatus.setId(ShippingStatus.Value.DELIVERED.getId());

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));
        when(shippingStatusRepository.getReferenceById(ShippingStatus.Value.DELIVERED.getId()))
                .thenReturn(deliveredStatus);

        adminShippingService.finishShipping(shippingId);

        verify(shippingRepository).findById(shippingId);
        verify(shippingStatusRepository).getReferenceById(ShippingStatus.Value.DELIVERED.getId());
        verify(shippingRepository).save(shipping);
    }

    @Test @DisplayName("Should throw ShippingNotFoundException if shipping not exists by ID")
    void finishShippingTestCase2() {
        UUID shippingId = UUID.randomUUID();

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> {
            adminShippingService.finishShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw CantTransitionShippingStatusException if shipping is not in transit")
    void finishShippingTestCase3() {
        UUID shippingId = UUID.randomUUID();

        var shippingStatus = new ShippingStatus();
        shippingStatus.setId(ShippingStatus.Value.DISPATCHED.getId());

        var salesOrderStatus = new SalesOrderStatus();
        salesOrderStatus.setId(SalesOrderStatus.Value.CONFIRMED.getId());

        var salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(salesOrderStatus);

        var shipping = new Shipping();
        shipping.setId(shippingId);
        shipping.setStatus(shippingStatus);
        shipping.setSalesOrder(salesOrder);

        when(shippingRepository.findById(shippingId))
                .thenReturn(Optional.of(shipping));

        assertThrows(CantTransitionShippingStatusException.class, () -> {
            adminShippingService.finishShipping(shippingId);
        });

        verify(shippingRepository).findById(shippingId);
        verify(shippingRepository, never()).save(any());
    }
}