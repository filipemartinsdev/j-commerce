package com.products.application.service;

import com.products.application.dto.catalogue.WishlistItemResponse;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.UUID;

public interface WishlistService {
    Window<WishlistItemResponse> get(UUID userId, ScrollPosition position, Limit limit);

    void add(UUID userId, String productId);

    void remove(UUID userId, String productId);

    void clear(UUID userId);
}
