package com.products.application.service.mapper;

import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.domain.entity.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishlistItemMapper {
    public WishlistItemResponse toResponse(WishlistItem entity) {
        return new WishlistItemResponse(
                entity.getProductId(),
                entity.getProductName()
        );
    }
}
