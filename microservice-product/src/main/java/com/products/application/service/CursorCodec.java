package com.products.application.service;

public interface CursorCodec {
    <T> String encode(T cursor);

    <T> T decode(String opaqueCursor, Class<T> type);
}
