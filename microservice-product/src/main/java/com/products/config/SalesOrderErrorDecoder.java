package com.products.config;

import com.products.application.exception.BadGatewayException;
import com.products.application.exception.DeliveryAddressNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SalesOrderErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404, 400 -> new DeliveryAddressNotFoundException("Delivery address not found");
            default -> {
                log.error("Bad gateway on Order Microservice, with status {}: {}", response.status(), response.body());
                yield new BadGatewayException("External service not responding");
            }
        };
    }
}