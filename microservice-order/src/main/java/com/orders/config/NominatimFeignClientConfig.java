package com.orders.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class NominatimFeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new NominatimFeignClientErrorDecoder();
    }
}
