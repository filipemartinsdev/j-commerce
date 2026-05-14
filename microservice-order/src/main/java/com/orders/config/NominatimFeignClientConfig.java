package com.orders.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class NominatimFeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new NominatimFeignClientErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("User-Agent", "JCommerce/1.0 (dev.filipemartins@gmail.com)");
            requestTemplate.header("Referer", "https://github.com/filipemartinsdev/j-commerce");
        };
    }
}
