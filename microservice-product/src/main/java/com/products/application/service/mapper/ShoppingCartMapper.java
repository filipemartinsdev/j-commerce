package com.products.application.service.mapper;

import com.products.domain.entity.ShoppingCart;
import com.products.application.dto.catalogue.ShoppingCartResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ShoppingCartMapper {

    public ShoppingCartResponse toResponse(ShoppingCart shoppingCart){
        return new ShoppingCartResponse(
                getTotalAmount(shoppingCart),
                shoppingCart.getItems().size(),
                toResponseItems(shoppingCart)
        );
    }

    private BigDecimal getTotalAmount(ShoppingCart shoppingCart) {
        return shoppingCart.getItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getUnits())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ShoppingCartResponse.Item> toResponseItems(ShoppingCart shoppingCart){
        return shoppingCart.getItems()
                .stream()
                .map(item -> new ShoppingCartResponse.Item(
                        item.getSKU(),
                        item.getName(),
                        item.getUnits(),
                        item.getPrice(),
                        item.getPrice().multiply(new BigDecimal(item.getUnits()))
                ))
                .toList();
    }
}
