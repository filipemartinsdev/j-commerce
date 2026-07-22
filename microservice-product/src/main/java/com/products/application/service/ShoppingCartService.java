package com.products.application.service;

import com.products.application.dto.catalogue.*;
import com.products.application.exception.*;
import com.products.application.message.OrderCheckedMessage;
import com.products.application.service.mapper.ShoppingCartMapper;
import com.products.domain.entity.ProductSKUPrice;
import com.products.infra.messaging.MessageBrokerProducer;
import com.products.infra.persistence.ProductSKUPriceRepository;
import com.products.infra.persistence.ProductSKURepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

 // TODO: update unit tests

@Slf4j
@Service
public class ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final ProductSKURepository productSKURepository;
    private final ProductStockChecker productStockChecker;
    private final MessageBrokerProducer messageBrokerProducer;
    private final StockMovementManagementService stockMovementService;
    private final ProductStockManagementService productStockManagementService;
    private final ShoppingCartCacheStorage shoppingCartCacheStorage;
    private final ProductSKUPriceRepository productSKUPriceRepository;
    private final SalesOrderClient salesOrderClient;

    public ShoppingCartService(ShoppingCartMapper shoppingCartMapper, ProductSKURepository productSKURepository, ProductStockChecker productStockChecker, MessageBrokerProducer messageBrokerProducer, StockMovementManagementService stockMovementService, ProductStockManagementService productStockManagementService, ShoppingCartCacheStorage shoppingCartCacheStorage, ProductSKUPriceRepository productSKUPriceRepository, SalesOrderClient salesOrderClient) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.productSKURepository = productSKURepository;
        this.productStockChecker = productStockChecker;
        this.messageBrokerProducer = messageBrokerProducer;
        this.stockMovementService = stockMovementService;
        this.productStockManagementService = productStockManagementService;
        this.shoppingCartCacheStorage = shoppingCartCacheStorage;
        this.productSKUPriceRepository = productSKUPriceRepository;
        this.salesOrderClient = salesOrderClient;
    }

    public void createItemByUserId(CreateShoppingCartItemRequest request, UUID userId) {
        if (!productSKURepository.existsById(request.productSKUId()))
            throw new ProductSKUNotFoundException("Product SKU not found with ID: "+request.productSKUId());

        if (!productStockChecker.isTheProductWithStockEnough(request.productSKUId(), request.units()))
            throw new ProductOutOfStockException("This product haven't stock enough");

        ProductSKUPrice productPrice = productSKUPriceRepository.findFirstCurrentPrice(request.productSKUId())
                .orElseThrow(() -> new ProductSKUPriceNotFoundException("Price not found"));

        var shoppingCart = shoppingCartCacheStorage.get(userId);

        for (ShoppingCart.Item item : shoppingCart.items())
            if (item.productSKUId().equals(request.productSKUId()))
                throw new ShoppingCartItemAlreadyExistsException("This product is already on shopping cart");

        shoppingCart.items().add(
                new ShoppingCart.Item(
                        request.productSKUId(),
                        productPrice.getProductSKU().getProduct().getId(),
                        productPrice.getProductSKU().getName(),
                        request.units(),
                        productPrice.getPrice()
                )
        );

        shoppingCartCacheStorage.update(userId, shoppingCart);
    }

    public ShoppingCartResponse getAllItems(UUID userId) {
        var shoppingCart = shoppingCartCacheStorage.get(userId);

        return new ShoppingCartResponse(
                shoppingCart.items().stream()
                        .map(item -> item.price().multiply(new BigDecimal(item.units())))
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO),
                shoppingCart.items()
        );
    }

    public void deleteItemByProductSKUId(UUID productSKUId, UUID userId) {
        var shoppingCart = shoppingCartCacheStorage.get(userId);

        shoppingCart.items().stream()
                .filter(shoppingCartItem -> shoppingCartItem.productSKUId().equals(productSKUId))
                .findFirst()
                .ifPresentOrElse(shoppingCart.items()::remove, () -> {
                    throw new ShoppingCartItemNotFoundException("ShoppingCart item not found with productSKUId: " + productSKUId);
                });


        shoppingCartCacheStorage.update(userId, shoppingCart);
    }

    public void deleteAllItemsByUserId(UUID userId) {
        shoppingCartCacheStorage.clear(userId);
    }

    @Transactional
    public void confirmShoppingCart(ConfirmShoppingCartRequest request, UUID userId, String JWTBearer) {
        verifyDeliveryAddress(request.deliveryAddressId(), JWTBearer);

        var shoppingCart = shoppingCartCacheStorage.get(userId);

        if (shoppingCart.items().isEmpty())
            throw new EmptyShoppingCartException("The shopping cart is empty");

        updateStock(shoppingCart.items(), userId);

        publishOrderCheckedMessage(shoppingCart, userId, request.deliveryAddressId());

        shoppingCartCacheStorage.clear(userId);
    }

    private void verifyDeliveryAddress(UUID deliveryAddressId, String JWTBearer) {
        salesOrderClient.getDeliveryAddress(deliveryAddressId, JWTBearer);
    }
    private void updateStock(List<ShoppingCart.Item> items, UUID userId) {
        for (var item : items) {
            productStockManagementService.decreaseProductStock(item.productSKUId(), item.units());
            stockMovementService.registerSale(item.productSKUId(), item.units(), userId);
        }
    }

    private void publishOrderCheckedMessage(ShoppingCart shoppingCart, UUID userId, UUID deliveryAddressId) {
        OrderCheckedMessage orderCheckedMessage = this.toOrderCheckedMessage(shoppingCart, userId, deliveryAddressId);
        messageBrokerProducer.produceOrderChecked(orderCheckedMessage);
    }

    private OrderCheckedMessage toOrderCheckedMessage(ShoppingCart shoppingCart, UUID userId, UUID deliveryAddressId) {
        return new OrderCheckedMessage(
                userId,
                shoppingCart.items().stream()
                        .map(item ->
                                new OrderCheckedMessage.OrderItem(
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
