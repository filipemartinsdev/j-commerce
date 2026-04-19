package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.service.ProductDiscountCalculator;
import com.products.domain.entity.ShoppingCartItemProductSKUResume;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ShoppingCartItemMapper {
    private final ProductDiscountCalculator productDiscountCalculator;

    public ShoppingCartItemMapper(ProductDiscountCalculator productDiscountCalculator) {
        this.productDiscountCalculator = productDiscountCalculator;
    }

    public ShoppingCartItemResponse toResponse(ShoppingCartItemProductSKUResume entity){
        return new ShoppingCartItemResponse(
                entity.getId(),
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                entity.getUnits(),
                entity.getOriginalPrice(),
                entity.getCurrentPrice() == null ? BigDecimal.ZERO : entity.getCurrentPrice(),
                productDiscountCalculator.getDiscountPercent(
                        entity.getOriginalPrice(),
                        entity.getCurrentPrice()
                )
        );
    }
}
