package com.orders.config;

import com.orders.application.exception.BadGatewayException;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import com.orders.application.exception.InvalidGeocodingResponseException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphHopperFeignClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()){
            case 404, 400 -> new InvalidGeocodingResponseException("Invalid geocoding response");
            default -> {
                log.error("GraphHopper invalid response with status code {}: {}", response.status(), response.body());
                yield new BadGatewayException("GraphHopper Bad Gateway with status code " + response.status());
            }
        };
    }
}
