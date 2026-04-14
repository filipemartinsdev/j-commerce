package com.products.application.event;

import com.products.application.service.AdminProductService;
import com.products.application.service.ProductStockService;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ProductSKUDeletedEventListener implements ApplicationListener<ProductSKUDeletedEvent> {
    private final ProductStockService productStockService;

    public ProductSKUDeletedEventListener(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @Override @Async
    public void onApplicationEvent(ProductSKUDeletedEvent event) {
        productStockService.deleteByProductSKUId(event.getProductSKUId());
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
