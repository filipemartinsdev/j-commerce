package com.orders.application.service;

import com.orders.application.dto.RouteResponse;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import com.orders.application.exception.InvalidRouteResponseException;
import com.orders.domain.entity.StorageAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryDateCalculatorImplTests {
    @Mock private StorageAddressService storageAddressService;
    @Mock private RouteService routeService;

    @InjectMocks DeliveryDateCalculatorImpl deliveryDateCalculatorImpl;

    @Test @DisplayName("Should create delivery date after now")
    void getDeliveryDateTestCase1() {
        Double[] storagePoint = new Double[]{-23.0, -46.0};
        when(storageAddressService.getMainStorageAddressPoint()).thenReturn(storagePoint);

        when(routeService.route(any(), any()))
                .thenReturn(new RouteService.Route(1000L, 5000L));

        Instant result = deliveryDateCalculatorImpl.getDeliveryDate(-23.5, -46.5);

        assertNotNull(result);
        assertTrue(result.isAfter(Instant.now()));
        verify(storageAddressService).getMainStorageAddressPoint();
        verify(routeService).route(any(), any());
    }

    @Test @DisplayName("Should create delivery date to one day after now")
    void getDeliveryDateTestCase2() {
        Double[] storagePoint = new Double[]{-23.0, -46.0};
        when(storageAddressService.getMainStorageAddressPoint()).thenReturn(storagePoint);

        when(routeService.route(any(), any()))
                .thenReturn(new RouteService.Route(0L, 0L));

        Instant result = deliveryDateCalculatorImpl.getDeliveryDate(-23.5, -46.5);

        assertNotNull(result);
        Instant oneDayFromNow = Instant.now().plusSeconds(86400);
        assertTrue(result.isAfter(Instant.now()));
        assertTrue(result.isBefore(oneDayFromNow.plusSeconds(1)));
    }

    @Test @DisplayName("Should throw InvalidRouteResponseException if route response is invalid")
    void getDeliveryDateTestCase3() {
        Double[] storagePoint = new Double[]{-23.0, -46.0};
        when(storageAddressService.getMainStorageAddressPoint()).thenReturn(storagePoint);

        when(routeService.route(any(), any()))
                .thenThrow(new InvalidRouteResponseException());

        assertThrows(InvalidRouteResponseException.class, () -> {
            deliveryDateCalculatorImpl.getDeliveryDate(-23.5, -46.5);
        });
    }
}