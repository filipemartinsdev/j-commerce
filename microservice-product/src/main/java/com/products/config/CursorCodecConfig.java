package com.products.config;

import com.products.application.service.CursorCodec;
import com.products.application.service.CursorCodecImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class CursorCodecConfig {

    @Bean
    public CursorCodec cursorCodec(ObjectMapper objectMapper){
        return new CursorCodecImpl(objectMapper);
    }
}
