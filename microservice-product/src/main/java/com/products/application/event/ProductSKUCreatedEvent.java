package com.products.application.event;

import com.products.domain.entity.ProductSKU;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ProductSKUCreatedEvent extends ApplicationEvent {
    private final ProductSKU sku;
    private final UUID userId;

    public ProductSKUCreatedEvent(ProductSKU sku, UUID userId, Object source) {
        super(source);
        this.sku = sku;
        this.userId = userId;
    }
}
