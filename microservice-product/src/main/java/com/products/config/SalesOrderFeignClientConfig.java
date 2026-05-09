package com.products.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class SalesOrderFeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new SalesOrderErrorDecoder();
    }
}
