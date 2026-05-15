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
    @Value("${graphHopperClient.apiKey}")
    private String apiKey;

    private final GraphHopperClient graphHopperClient;

    public DeliveryDateCalculatorImpl(GraphHopperClient graphHopperClient, StorageAddressService storageAddressService) {
        this.graphHopperClient = graphHopperClient;
        this.storageAddressService = storageAddressService;
    }

    @Override
    public Instant getDeliveryDate(Double lat, Double lon) {
        Double[] shoppingPoint = storageAddressService.getMainStorageAddressPoint();

        Double[][] points = new Double[2][2];

        /*
        * The GraphHopper API needs the inverted default points, then:
        * [lon, lat] instead of [lat, lon]
        * **/

        points[0][0] = shoppingPoint[1];
        points[0][1] = shoppingPoint[0];

        points[1][0] = lon;
        points[1][1] = lat;

        RouteRequest request = new RouteRequest("car", points);

        ResponseEntity<RouteResponse> routeResponse = graphHopperClient.route(
                apiKey, request
        );

        if (!isRouteResponseValid(routeResponse.getBody())) {
            throw new InvalidRouteResponseException();
        }

        return Instant.now()
                .plusSeconds(86400) // 24 Hours
                .plusMillis(routeResponse.getBody().paths().getFirst().time());
    }

    private boolean isRouteResponseValid(RouteResponse routeResponse) {
        return routeResponse != null &&
                routeResponse.paths() != null &&
                !routeResponse.paths().isEmpty() &&
                routeResponse.paths().getFirst() != null;
    }
}
