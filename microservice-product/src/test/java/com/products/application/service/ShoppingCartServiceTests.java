package com.products.application.service;

import com.products.application.dto.catalogue.ConfirmShoppingCartRequest;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCart;
import com.products.application.dto.catalogue.ShoppingCartResponse;
import com.products.application.exception.EmptyShoppingCartException;
import com.products.application.exception.ProductOutOfStockException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ShoppingCartItemAlreadyExistsException;
import com.products.application.exception.ShoppingCartItemNotFoundException;
import com.products.application.message.OrderCheckedMessage;
import com.products.application.service.mapper.ShoppingCartMapper;
import com.products.domain.entity.*;
import com.products.infra.messaging.MessageBrokerProducer;
import com.products.infra.persistence.ProductSKUPriceRepository;
import com.products.infra.persistence.ProductSKURepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTests {
    @Mock private ShoppingCartMapper shoppingCartMapper;
    @Mock private ProductSKURepository productSKURepository;
    @Mock private ProductStockChecker productStockChecker;
    @Mock private MessageBrokerProducer shoppingCartConfirmationProducer;
    @Mock private StockMovementManagementService stockMovementService;
    @Mock private ProductStockManagementService productStockManagementService;
    @Mock private ShoppingCartCacheStorage shoppingCartCacheStorage;
    @Mock private ProductSKUPriceRepository productSKUPriceRepository;
    @Mock private SalesOrderClient salesOrderClient;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Test
    @DisplayName("Should create item successfully")
    void createItemByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        var request = new CreateShoppingCartItemRequest(productSKUId, 2);

        var product = new Product();
        product.setId(UUID.randomUUID());

        var productSKU = new ProductSKU();
        productSKU.setId(productSKUId);
        productSKU.setProduct(product);

        var price = new ProductSKUPrice();
        price.setProductSKU(productSKU);
        price.setPrice(BigDecimal.ONE);

        var shoppingCart = new ShoppingCart(List.of(
                new ShoppingCart.Item(productSKUId, product.getId(), productSKU.getName(), 2, price.getPrice())
        ));

        when(productSKURepository.existsById(request.productSKUId()))
                .thenReturn(true);
        when(productStockChecker.isTheProductWithStockEnough(request.productSKUId(), request.units()))
                .thenReturn(true);
        when(productSKUPriceRepository.findFirstCurrentPrice(request.productSKUId()))
                .thenReturn(Optional.of(price));
        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(new ShoppingCart(new ArrayList<>()));
        when(shoppingCartCacheStorage.update(userId, shoppingCart))
                .thenReturn(shoppingCart);

        shoppingCartService.createItemByUserId(request, userId);

        verify(shoppingCartCacheStorage)
                .get(userId);
        verify(shoppingCartCacheStorage)
                .update(userId, shoppingCart);
    }

    @Test
    @DisplayName("Should throw ProductSKUNotFoundException when SKU not found")
    void createItemByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        var request = new CreateShoppingCartItemRequest(productSKUId, 2);

        when(productSKURepository.existsById(productSKUId))
                .thenReturn(false);

        assertThrows(ProductSKUNotFoundException.class, () ->
                shoppingCartService.createItemByUserId(request, userId)
        );
    }

    @Test
    @DisplayName("Should throw ProductOutOfStockException when insufficient stock")
    void createItemByUserIdTestCase3() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();
        var units = 10;

        var request = new CreateShoppingCartItemRequest(productSKUId, units);

        var productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        when(productSKURepository.existsById(productSKUId))
                .thenReturn(true);
        when(productStockChecker.isTheProductWithStockEnough(productSKUId, units))
                .thenReturn(false);

        assertThrows(ProductOutOfStockException.class, () ->
                shoppingCartService.createItemByUserId(request, userId)
        );
    }

    @Test
    @DisplayName("Should throw ShoppingCartItemAlreadyExistsException when item exists")
    void createItemByUserIdTestCase4() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();
        var units = 10;

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, units);

        var product = new Product();
        product.setId(UUID.randomUUID());

        var productSKU = new ProductSKU();
        productSKU.setId(productSKUId);
        productSKU.setProduct(product);

        var price = new ProductSKUPrice();
        price.setPrice(BigDecimal.ONE);

        var shoppingCart = new ShoppingCart(List.of(
                new ShoppingCart.Item(
                        productSKUId,
                        productSKU.getProduct().getId(),
                        "test",
                        units,
                        price.getPrice()
                )
        ));

        when(productSKURepository.existsById(productSKUId))
                .thenReturn(true);
        when(productStockChecker.isTheProductWithStockEnough(productSKUId, units))
                .thenReturn(true);
        when(productSKUPriceRepository.findFirstCurrentPrice(productSKUId))
                .thenReturn(Optional.of(price));
        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(shoppingCart);

        assertThrows(ShoppingCartItemAlreadyExistsException.class, () ->
                shoppingCartService.createItemByUserId(request, userId)
        );
    }

    @Test
    @DisplayName("Should return shopping-cart successfully")
    void getAllItemsTestCase1() {
        UUID userId = UUID.randomUUID();
        var units = 10;

        var product = new Product();
        product.setId(UUID.randomUUID());

        var productSKU = new ProductSKU();
        productSKU.setId(UUID.randomUUID());
        productSKU.setProduct(product);

        var price = new ProductSKUPrice();
        price.setPrice(BigDecimal.ONE);

        var shoppingCart = new ShoppingCart(List.of(
                new ShoppingCart.Item(
                        productSKU.getId(),
                        productSKU.getProduct().getId(),
                        "test",
                        units,
                        price.getPrice()
                )
        ));

        BigDecimal totalValue = price.getPrice().multiply(new BigDecimal(units));


        var expectedResponse = new ShoppingCartResponse(totalValue, shoppingCart.items());

        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(shoppingCart);

        ShoppingCartResponse result = shoppingCartService.getAllItems(userId);

        assertEquals(expectedResponse, result);
    }

    @Test
    @DisplayName("Should delete item successfully")
    void deleteItemByProductSKUIdTestCase1() {
        UUID userId = UUID.randomUUID();

        var product = new Product();
        product.setId(UUID.randomUUID());

        var productSKU = new ProductSKU();
        productSKU.setId(UUID.randomUUID());
        productSKU.setProduct(product);

        var shoppingCart = new ShoppingCart(new ArrayList<>(List.of(
                new ShoppingCart.Item(
                        productSKU.getId(),
                        product.getId(),
                        productSKU.getName(),
                        1,
                        BigDecimal.ONE
                )
        )));

        var emptyShoppingCart = new ShoppingCart(List.of());

        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(shoppingCart);
        when(shoppingCartCacheStorage.update(userId, emptyShoppingCart))
                .thenReturn(emptyShoppingCart);

        shoppingCartService.deleteItemByProductSKUId(productSKU.getId(), userId);

        verify(shoppingCartCacheStorage)
                .get(userId);
        verify(shoppingCartCacheStorage)
                .update(userId, emptyShoppingCart);
    }

    @Test
    @DisplayName("Should throw ShoppingCartItemNotFoundException when item not found")
    void deleteItemByProductSKUIdTestCase2() {
        UUID itemId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(new ShoppingCart(List.of()));

        assertThrows(ShoppingCartItemNotFoundException.class, () ->
                shoppingCartService.deleteItemByProductSKUId(itemId, userId)
        );
    }

    @Test
    @DisplayName("Should delete all items by user ID")
    void deleteAllItemsByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();

        doNothing()
                .when(shoppingCartCacheStorage).clear(userId);

        shoppingCartService.deleteAllItemsByUserId(userId);

        verify(shoppingCartCacheStorage).clear(userId);
    }

    @Test
    @DisplayName("Should confirm shopping cart successfully")
    void confirmShoppingCartTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();
        var jwtBearer = "Bearer token";

        var request = new ConfirmShoppingCartRequest(deliveryAddressId);

        var shoppingCart = new ShoppingCart(new ArrayList<>(List.of(
                new ShoppingCart.Item(
                        null,
                        null,
                        null,
                        1,
                        null
                )
        )));

        when(salesOrderClient.getDeliveryAddress(deliveryAddressId, jwtBearer))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(shoppingCart);
        doNothing()
                .when(productStockManagementService).decreaseProductStock(any(), anyInt());
        doNothing()
                .when(stockMovementService).registerSale(any(), anyInt(), any());
        doNothing()
                .when(shoppingCartConfirmationProducer).produceOrderChecked(any());
        doNothing()
                .when(shoppingCartCacheStorage).clear(userId);

        shoppingCartService.confirmShoppingCart(request, userId, jwtBearer);

        verify(shoppingCartCacheStorage)
                .get(userId);
        verify(shoppingCartCacheStorage)
                .clear(userId);
        verify(shoppingCartConfirmationProducer)
                .produceOrderChecked(any());
    }

    @Test
    @DisplayName("Should throw EmptyShoppingCartException when cart is empty")
    void confirmShoppingCartTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();
        String jwtBearer = "Bearer token";

        ConfirmShoppingCartRequest request = new ConfirmShoppingCartRequest(deliveryAddressId);

        when(salesOrderClient.getDeliveryAddress(deliveryAddressId, jwtBearer))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(shoppingCartCacheStorage.get(userId))
                .thenReturn(new ShoppingCart(List.of()));

        assertThrows(EmptyShoppingCartException.class, () ->
                shoppingCartService.confirmShoppingCart(request, userId, jwtBearer)
        );
    }
}