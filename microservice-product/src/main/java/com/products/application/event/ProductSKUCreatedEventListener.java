package com.products.application.event;

import com.products.application.service.ProductStockService;
import com.products.domain.entity.ProductStock;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ProductSKUCreatedEventListener implements ApplicationListener<ProductSKUCreatedEvent> {
    private final ProductStockService productStockService;

    public ProductSKUCreatedEventListener(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @Override
    public void onApplicationEvent(ProductSKUCreatedEvent event) {
        productStockService.createStockToSKU(event.getSku(), event.getUserId());
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
