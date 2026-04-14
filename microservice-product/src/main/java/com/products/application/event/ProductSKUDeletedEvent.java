package com.products.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ProductSKUDeletedEvent extends ApplicationEvent {
    private final UUID productSKUId;

    public ProductSKUDeletedEvent(UUID productSKUId, Object source) {
        super(source);
        this.productSKUId = productSKUId;
    }
}
