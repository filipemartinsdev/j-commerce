package com.orders.config;

import com.orders.application.exception.BadGatewayException;
import com.orders.application.exception.InvalidDeliveryAddressCoordinatesException;
import com.orders.application.exception.InvalidGeocodingResponseException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class GraphHopperFeignClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()){
            case 404, 400 -> {
                try {
                    String bodyString = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                    log.warn("Invalid geocoding response body, with status {}: {}", response.status(), bodyString);
                }
                catch (IOException e) {
                    log.error("Error reading response body", e);
                } finally {
                    yield new InvalidGeocodingResponseException("Invalid geocoding response");
                }
            }

            default -> {
                try {
                    String bodyString = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                    log.error("GraphHopper invalid response with status code {}: {}", response.status(), bodyString);
                }
                catch (IOException e) {
                    log.error("Error reading response body", e);
                }
                finally {
                    yield new BadGatewayException("GraphHopper Bad Gateway with status code " + response.status());
                }
            }
        };
    }
}
