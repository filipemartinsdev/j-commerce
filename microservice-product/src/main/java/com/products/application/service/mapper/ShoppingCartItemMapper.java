package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.message.CreateOrderMessage;
import com.products.application.service.ProductDiscountCalculator;
import com.products.domain.entity.ShoppingCartItemSummaryView;
import org.springframework.stereotype.Component;

@Component
public class ShoppingCartItemMapper {
    private final ProductDiscountCalculator productDiscountCalculator;

    public ShoppingCartItemMapper(ProductDiscountCalculator productDiscountCalculator) {
        this.productDiscountCalculator = productDiscountCalculator;
    }

    public ShoppingCartItemResponse toResponse(ShoppingCartItemSummaryView entity){
        return new ShoppingCartItemResponse(
                entity.getId(),
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                entity.getUnits(),
                entity.getOriginalPrice(),
                entity.getCurrentPrice() == null ? entity.getOriginalPrice() : entity.getCurrentPrice(),
                productDiscountCalculator.getDiscountPercent(
                        entity.getOriginalPrice(),
                        entity.getCurrentPrice()
                )
        );
    }

    public CreateOrderMessage.OrderItem toCreateOrderMessageItem(ShoppingCartItemSummaryView entity) {
        return new CreateOrderMessage.OrderItem(
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                entity.getUnits(),
                entity.getCurrentPrice() == null ? entity.getOriginalPrice() : entity.getCurrentPrice()
        );
    }
}
