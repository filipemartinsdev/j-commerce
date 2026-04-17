package com.products.application.event;

import com.products.application.service.AdminProductStockService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ProductSKUCreatedEventListener implements ApplicationListener<ProductSKUCreatedEvent> {
    private final AdminProductStockService adminProductStockService;

    public ProductSKUCreatedEventListener(AdminProductStockService adminProductStockService) {
        this.adminProductStockService = adminProductStockService;
    }

    @Override
    public void onApplicationEvent(ProductSKUCreatedEvent event) {
        adminProductStockService.createStockToSKU(event.getSku(), event.getUserId());
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
