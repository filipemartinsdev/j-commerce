package com.orders.config;

import com.orders.application.exception.BadGatewayException;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NominatimFeignClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()){
            case 404, 400 -> new InvalidDeliveryAddressCoordinatesException("Invalid coordinates");
            default -> {
                log.error("Nominatim invalid response with status code {}: {}", response.status(), response.body());
                yield new BadGatewayException("Nominatim Bad Gateway with status code " + response.status());
            }
        };
    }
}
