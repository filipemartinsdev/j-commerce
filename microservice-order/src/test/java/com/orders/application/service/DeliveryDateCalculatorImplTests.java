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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeliveryDateCalculatorImplTests {
    @Mock private StorageAddressService storageAddressService;
    @Mock private GraphHopperClient graphHopperClient;

    @InjectMocks DeliveryDateCalculatorImpl deliveryDateCalculatorImpl;

    @Test @DisplayName("Should create delivery date after now")
    void getDeliveryDateTestCase1() {
//        Given
        Double lat = -14D;
        Double lon = -50D;

        StorageAddress storageAddress = new StorageAddress();
        storageAddress.setLatitude(lat);
        storageAddress.setLongitude(lon);

        when(storageAddressService.getMainStorageAddressPoint())
                .thenReturn(new Double[]{storageAddress.getLatitude(), storageAddress.getLongitude()});

        when(graphHopperClient.route(any(), any()))
                .thenReturn(
                        ResponseEntity.ok(
                                new RouteResponse(List.of(new RouteResponse.Path(0L, 0L)))
                        )
                );

        Instant minExpectedDeliveryDate = Instant.now();

//        When
        Instant deliveryDate = deliveryDateCalculatorImpl.getDeliveryDate(lat, lon);

//        Then
        assertNotNull(deliveryDate);
        assertTrue(deliveryDate.isAfter(minExpectedDeliveryDate));
    }

    @Test @DisplayName("Should create delivery date to one day after now")
    void getDeliveryDateTestCase2() {
//        Given
        Double lat = -14D;
        Double lon = -50D;

        StorageAddress storageAddress = new StorageAddress();
        storageAddress.setLatitude(lat);
        storageAddress.setLongitude(lon);

        when(storageAddressService.getMainStorageAddressPoint())
                .thenReturn(new Double[]{storageAddress.getLatitude(), storageAddress.getLongitude()});

        when(graphHopperClient.route(any(), any()))
                .thenReturn(
                        ResponseEntity.ok(
                                new RouteResponse(List.of(new RouteResponse.Path(0L, 0L)))
                        )
                );

        long oneHourOnSeconds = 86400L;
        Instant minExpectedDeliveryDate = Instant.now().plusSeconds(oneHourOnSeconds);

//        When
        Instant deliveryDate = deliveryDateCalculatorImpl.getDeliveryDate(lat, lon);

//        Then
        assertNotNull(deliveryDate);
        assertTrue(deliveryDate.isAfter(minExpectedDeliveryDate));
    }

    @Test @DisplayName("Should throw InvalidRouteResponseException if route response is invalid")
    void getDeliveryDateTestCase3() {
//        Brazilian coordinates
        Double lat = 0D;
        Double lon = 0D;

        StorageAddress storageAddress = new StorageAddress();
        storageAddress.setLatitude(lat);
        storageAddress.setLongitude(lon);

        when(storageAddressService.getMainStorageAddressPoint())
                .thenReturn(new Double[]{storageAddress.getLatitude(), storageAddress.getLongitude()});

        when(graphHopperClient.route(any(), any()))
                .thenReturn(
                        ResponseEntity.ok(
                                new RouteResponse(List.of())
                        )
                );

        assertThrows(InvalidRouteResponseException.class, () -> {
            deliveryDateCalculatorImpl.getDeliveryDate(lat, lon);
        });
    }
}