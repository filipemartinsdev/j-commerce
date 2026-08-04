package com.products.application.service;

import com.products.application.dto.catalogue.ShoppingCartResponse;
import com.products.application.exception.*;
import com.products.application.message.OrderCheckedMessage;
import com.products.application.service.mapper.ShoppingCartMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ShoppingCart;
import com.products.infra.feign.SalesOrderClient;
import com.products.infra.messaging.MessageBrokerProducer;
import com.products.infra.persistence.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartCacheStorage shoppingCartCacheStorage;
    private final ProductRepository productRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final SalesOrderClient salesOrderClient;
    private final MessageBrokerProducer messageBrokerProducer;

    public ShoppingCartServiceImpl(ShoppingCartCacheStorage shoppingCartCacheStorage, ProductRepository productRepository, ShoppingCartMapper shoppingCartMapper, SalesOrderClient salesOrderClient, MessageBrokerProducer messageBrokerProducer) {
        this.shoppingCartCacheStorage = shoppingCartCacheStorage;
        this.productRepository = productRepository;
        this.shoppingCartMapper = shoppingCartMapper;
        this.salesOrderClient = salesOrderClient;
        this.messageBrokerProducer = messageBrokerProducer;
    }

    @Override
    public ShoppingCartResponse get(UUID userId) {
        return shoppingCartMapper.toResponse(
                shoppingCartCacheStorage.get(userId)
        );
    }

    @Override
    public void add(UUID userId, String SKU, int units) {
        var shoppingCart = shoppingCartCacheStorage.get(userId);

        if(isSkuOnShoppingCart(SKU, shoppingCart))
            throw new ShoppingCartItemAlreadyExistsException("This product is already on shopping cart");

        Product.ProductSKU sku = productRepository.findSKU(SKU)
                .orElseThrow(() -> new ProductSKUNotFoundException("SKU not found: "+SKU))
                .getSKUs().getFirst();

        if (sku.getStock() < units)
            throw new ProductOutOfStockException("This product haven't stock enough");


        shoppingCart.getItems().add(
                new ShoppingCart.Item(
                        sku.getSKU(),
                        sku.getName(),
                        units,
                        sku.getCurrentPrice().getValue()
                )
        );

        shoppingCartCacheStorage.update(userId, shoppingCart);
    }

    private boolean isSkuOnShoppingCart(String SKU, ShoppingCart shoppingCart) {
        for (ShoppingCart.Item item : shoppingCart.getItems())
            if (item.getSKU().equals(SKU))
                return true;
        return false;
    }

    @Override
    public void remove(UUID userId, String SKU) {
        var shoppingCart = shoppingCartCacheStorage.get(userId);

        boolean removed = shoppingCart.getItems()
                .removeIf(item -> item.getSKU().equals(SKU));

        if (removed)
            shoppingCartCacheStorage.update(userId, shoppingCart);
        else
            throw new ShoppingCartItemNotFoundException("Shopping cart item not found by SKU: "+SKU);
    }

    @Override
    public void clear(UUID userId) {
        shoppingCartCacheStorage.clear(userId);
    }

    @Override
    public void confirm(UUID userId, UUID deliveryAddressId, String JWTBearer) {
        verifyDeliveryAddress(deliveryAddressId, JWTBearer);

        var shoppingCart = shoppingCartCacheStorage.get(userId);

        if (shoppingCart.getItems().isEmpty())
            throw new EmptyShoppingCartException("The shopping cart is empty");

        publishOrderCheckedMessage(shoppingCart, userId, deliveryAddressId);

        shoppingCartCacheStorage.clear(userId);
    }

    private void verifyDeliveryAddress(UUID deliveryAddressId, String JWTBearer) {
        salesOrderClient.getDeliveryAddress(deliveryAddressId, JWTBearer);
    }

    private void publishOrderCheckedMessage(ShoppingCart shoppingCart, UUID userId, UUID deliveryAddressId) {
        OrderCheckedMessage orderCheckedMessage = this.toOrderCheckedMessage(shoppingCart, userId, deliveryAddressId);
        messageBrokerProducer.produceOrderChecked(orderCheckedMessage);
    }

    private OrderCheckedMessage toOrderCheckedMessage(ShoppingCart shoppingCart, UUID userId, UUID deliveryAddressId) {
        return new OrderCheckedMessage(
                userId,
                shoppingCart.getItems().stream()
                        .map(item ->
                                new OrderCheckedMessage.OrderItem(
                                        item.getSKU(),
                                        item.getName(),
                                        item.getUnits(),
                                        item.getPrice()
                                )
                        )
                        .toList(),
                deliveryAddressId
        );
    }
}
