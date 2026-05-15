package com.orders.application.service;

import com.orders.application.dto.RouteRequest;
import com.orders.application.dto.RouteResponse;
import com.orders.application.exception.InvalidRouteResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class DeliveryDateCalculatorImpl implements DeliveryDateCalculator {
    private final StorageAddressService storageAddressService;
    private final RouteService routeService;

    public DeliveryDateCalculatorImpl(StorageAddressService storageAddressService, RouteService routeService) {
        this.storageAddressService = storageAddressService;
        this.routeService = routeService;
    }

    @Override
    public Instant getDeliveryDate(Double lat, Double lon) {
        Double[] shoppingPoint = storageAddressService.getMainStorageAddressPoint();

        var pointA = new RouteService.Point(shoppingPoint[0], shoppingPoint[1]);
        var pointB = new RouteService.Point(lat, lon);

        return Instant.now()
                .plusSeconds(86400) // 24 Hours
                .plusMillis(routeService.route(pointA, pointB).timeInMilli());
    }
}
