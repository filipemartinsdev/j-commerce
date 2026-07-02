package com.products.application.service;

import com.products.application.exception.CursorDecodingException;
import com.products.application.exception.CursorEncodingException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

@Component
public class CursorCodecImpl implements CursorCodec {
    private final ObjectMapper objectMapper;

    public CursorCodecImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> String encode(T cursor) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e){
            throw new CursorEncodingException("Invalid cursor");
        }
    }

    @Override
    public <T> T decode(String opaqueCursor, Class<T> type) {
        if (opaqueCursor == null || opaqueCursor.isEmpty())
            throw new CursorDecodingException("Invalid cursor");

        try {
            byte[] bytes = Base64.getUrlDecoder().decode(opaqueCursor);
            return objectMapper.readValue(bytes, type);
        } catch (Exception e){
            throw new CursorDecodingException("Invalid cursor");
        }
    }
}
