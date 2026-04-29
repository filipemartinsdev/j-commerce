package com.products.application.event;

import com.products.application.service.ProductPriceManagementService;
import com.products.application.service.ProductStockManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductSKUDeletedEventListener implements ApplicationListener<ProductSKUDeletedEvent> {
    private final ProductStockManagementService adminProductStockService;
    private final ProductPriceManagementService adminProductPriceService;

    public ProductSKUDeletedEventListener(ProductStockManagementService adminProductStockService, ProductPriceManagementService adminProductPriceService) {
        this.adminProductStockService = adminProductStockService;
        this.adminProductPriceService = adminProductPriceService;
    }

    @Override @Async
    public void onApplicationEvent(ProductSKUDeletedEvent event) {
        adminProductStockService.deleteByProductSKUId(event.getProductSKUId());
        adminProductPriceService.deleteAllByProductSKUId(event.getProductSKUId());
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
