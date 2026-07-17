package com.products.application.service.mapper;

import com.products.application.dto.catalogue.ShoppingCart;
import com.products.application.dto.catalogue.ShoppingCartResponse;
import com.products.application.message.CreateOrderMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ShoppingCartMapper {

    public ShoppingCartResponse toResponse(ShoppingCart shoppingCart){
        return new ShoppingCartResponse(
                getTotalValue(shoppingCart),
                shoppingCart.items()
        );
    }

    private BigDecimal getTotalValue(ShoppingCart shoppingCart) {
        return shoppingCart.items().stream()
                .map(item -> item.price().multiply(new BigDecimal(item.units())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CreateOrderMessage toCreateOrderMessage(ShoppingCart shoppingCart, UUID userId, UUID deliveryAddressId) {
        return new CreateOrderMessage(
                userId,
                shoppingCart.items().stream()
                        .map(item ->
                            new CreateOrderMessage.OrderItem(
                                    item.productSKUId(),
                                    item.productSKUName(),
                                    item.units(),
                                    item.price()
                            )
                        )
                        .toList(),
                deliveryAddressId
        );
    }
}
