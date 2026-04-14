package com.products.application.event;

import com.products.application.service.AdminProductPriceService;
import com.products.application.service.AdminProductService;
import com.products.application.service.ProductStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductSKUDeletedEventListener implements ApplicationListener<ProductSKUDeletedEvent> {
    private final ProductStockService productStockService;
    private final AdminProductPriceService adminProductPriceService;

    public ProductSKUDeletedEventListener(ProductStockService productStockService, AdminProductPriceService adminProductPriceService) {
        this.productStockService = productStockService;
        this.adminProductPriceService = adminProductPriceService;
    }

    @Override @Async
    public void onApplicationEvent(ProductSKUDeletedEvent event) {
        productStockService.deleteByProductSKUId(event.getProductSKUId());
        adminProductPriceService.deleteAllByProductSKUId(event.getProductSKUId());
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
