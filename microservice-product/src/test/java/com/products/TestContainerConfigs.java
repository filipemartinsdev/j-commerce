package com.products;

import io.floci.testcontainers.FlociContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class TestContainerConfigs {
    @Bean
    public FlociContainer flociContainer(){
        return new FlociContainer();
    }
}
