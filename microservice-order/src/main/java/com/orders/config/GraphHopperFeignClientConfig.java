package com.orders.config;

import feign.codec.ErrorDecoder;
import org.hibernate.graph.Graph;
import org.springframework.context.annotation.Bean;

public class GraphHopperFeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new GraphHopperFeignClientErrorDecoder();
    }
}
