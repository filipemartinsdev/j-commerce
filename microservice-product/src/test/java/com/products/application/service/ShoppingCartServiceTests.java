package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.ConfirmShoppingCartRequest;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.exception.DeliveryAddressNotFoundException;
import com.products.application.exception.EmptyShoppingCartException;
import com.products.application.exception.ProductOutOfStockException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ShoppingCartItemAlreadyExistsException;
import com.products.application.exception.ShoppingCartItemNotFoundException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.message.CreateOrderMessage;
import com.products.application.service.mapper.ShoppingCartItemMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ShoppingCartItem;
import com.products.domain.entity.ShoppingCartItemProductSKUSummary;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.ShoppingCartItemProductSKUSummaryRepository;
import com.products.infra.persistence.ShoppingCartItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTests {

    @Mock
    private ShoppingCartItemRepository shoppingCartItemRepository;

    @Mock
    private ShoppingCartItemMapper shoppingCartItemProductSKUMapper;

    @Mock
    private ShoppingCartItemProductSKUSummaryRepository shoppingCartItemProductSKUSummaryRepository;

    @Mock
    private ProductSKURepository productSKURepository;

    @Mock
    private ProductStockChecker productStockChecker;

    @Mock
    private MessageBrokerProducer shoppingCartConfirmationProducer;

    @Mock
    private ProductStockManagementService productStockManagementService;

    @Mock
    private StockMovementManagementService stockMovementService;

    @Mock
    private SalesOrderClient salesOrderClient;

    @Mock
    private PagedResponseFactory<ShoppingCartItemResponse> pagedResponseFactory;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Test
    @DisplayName("Should create item successfully")
    void createItemByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 2);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        when(productSKURepository.findActiveById(productSKUId)).thenReturn(Optional.of(productSKU));
        when(productStockChecker.isTheProductWithStockEnough(productSKUId, 2)).thenReturn(true);
        when(shoppingCartItemRepository.existsByProductSKUIdAndUserId(productSKUId, userId)).thenReturn(false);
        when(shoppingCartItemRepository.save(any(ShoppingCartItem.class))).thenReturn(new ShoppingCartItem());

        shoppingCartService.createItemByUserId(request, userId);

        verify(shoppingCartItemRepository).save(any(ShoppingCartItem.class));
    }

    @Test
    @DisplayName("Should throw ProductSKUNotFoundException when SKU not found")
    void createItemByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 2);

        when(productSKURepository.findActiveById(productSKUId)).thenReturn(Optional.empty());

        assertThrows(ProductSKUNotFoundException.class, () ->
                shoppingCartService.createItemByUserId(request, userId)
        );
    }

    @Test
    @DisplayName("Should throw ProductOutOfStockException when insufficient stock")
    void createItemByUserIdTestCase3() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 10);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        when(productSKURepository.findActiveById(productSKUId)).thenReturn(Optional.of(productSKU));
        when(productStockChecker.isTheProductWithStockEnough(productSKUId, 10)).thenReturn(false);

        assertThrows(ProductOutOfStockException.class, () ->
                shoppingCartService.createItemByUserId(request, userId)
        );
    }

    @Test
    @DisplayName("Should throw ShoppingCartItemAlreadyExistsException when item exists")
    void createItemByUserIdTestCase4() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 2);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        when(productSKURepository.findActiveById(productSKUId)).thenReturn(Optional.of(productSKU));
        when(productStockChecker.isTheProductWithStockEnough(productSKUId, 2)).thenReturn(true);
        when(shoppingCartItemRepository.existsByProductSKUIdAndUserId(productSKUId, userId)).thenReturn(true);

        assertThrows(ShoppingCartItemAlreadyExistsException.class, () ->
                shoppingCartService.createItemByUserId(request, userId)
        );
    }

    @Test
    @DisplayName("Should return paginated items")
    void getAllItemsTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ShoppingCartItemProductSKUSummary entity = new ShoppingCartItemProductSKUSummary();
        entity.setId(UUID.randomUUID());
        entity.setProductSKUId(UUID.randomUUID());
        entity.setProductSKUName("Test");
        entity.setUnits(2);
        entity.setOriginalPrice(BigDecimal.valueOf(100));
        entity.setCurrentPrice(BigDecimal.valueOf(80));

        Page<ShoppingCartItemProductSKUSummary> page = new PageImpl<>(List.of(entity), pageable, 1);
        ShoppingCartItemResponse response = new ShoppingCartItemResponse(
                entity.getId(), entity.getProductSKUId(), entity.getProductSKUName(),
                entity.getUnits(), entity.getOriginalPrice(), entity.getCurrentPrice(), 20
        );

        PagedResponse<ShoppingCartItemResponse> expectedResponse = PagedResponse.<ShoppingCartItemResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(1L)
                .totalPages(1)
                .content(List.of(response))
                .build();

        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId, pageable)).thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        PagedResponse<ShoppingCartItemResponse> result = shoppingCartService.getAllItems(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test
    @DisplayName("Should return empty page when no items")
    void getAllItemsTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ShoppingCartItemProductSKUSummary> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        PagedResponse<ShoppingCartItemResponse> expectedResponse = PagedResponse.<ShoppingCartItemResponse>builder()
                .content(new java.util.ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId, pageable)).thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        PagedResponse<ShoppingCartItemResponse> result = shoppingCartService.getAllItems(userId, pageable);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test
    @DisplayName("Should delete item successfully")
    void deleteItemByIdTestCase1() {
        UUID itemId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ShoppingCartItem item = new ShoppingCartItem();
        item.setId(itemId);
        item.setIsActive(true);

        when(shoppingCartItemRepository.findActiveByIdAndUserId(itemId, userId)).thenReturn(Optional.of(item));
        when(shoppingCartItemRepository.save(any(ShoppingCartItem.class))).thenReturn(item);

        shoppingCartService.deleteItemById(itemId, userId);

        assertFalse(item.getIsActive());
        verify(shoppingCartItemRepository).save(item);
    }

    @Test
    @DisplayName("Should throw ShoppingCartItemNotFoundException when item not found")
    void deleteItemByIdTestCase2() {
        UUID itemId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(shoppingCartItemRepository.findActiveByIdAndUserId(itemId, userId)).thenReturn(Optional.empty());

        assertThrows(ShoppingCartItemNotFoundException.class, () ->
                shoppingCartService.deleteItemById(itemId, userId)
        );
    }

    @Test
    @DisplayName("Should delete all items by user ID")
    void deleteAllItemsByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();

        shoppingCartService.deleteAllItemsByUserId(userId);

        verify(shoppingCartItemRepository).markAllAsInactiveByUserId(userId);
    }

    @Test
    @DisplayName("Should confirm shopping cart successfully")
    void confirmShoppingCartTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();
        String jwtBearer = "Bearer token";

        ConfirmShoppingCartRequest request = new ConfirmShoppingCartRequest(deliveryAddressId);

        CreateOrderMessage.OrderItem orderItem = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(), "Product", 2, BigDecimal.valueOf(100)
        );

        ShoppingCartItemProductSKUSummary summary = new ShoppingCartItemProductSKUSummary();
        summary.setProductSKUId(UUID.randomUUID());
        summary.setProductSKUName("Product");
        summary.setUnits(2);
        summary.setCurrentPrice(BigDecimal.valueOf(100));

        when(salesOrderClient.getDeliveryAddress(deliveryAddressId, jwtBearer))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId)).thenReturn(List.of(summary));
        when(shoppingCartItemProductSKUMapper.toCreateOrderMessageItem(summary)).thenReturn(orderItem);

        shoppingCartService.confirmShoppingCart(request, userId, jwtBearer);

        verify(productStockManagementService).reduceProductStock(any(), anyInt());
        verify(stockMovementService).registerSale(any(), anyInt(), eq(userId));
        verify(shoppingCartConfirmationProducer).produce(any(CreateOrderMessage.class));
        verify(shoppingCartItemRepository).markAllAsInactiveByUserId(userId);
    }

    @Test
    @DisplayName("Should throw EmptyShoppingCartException when cart is empty")
    void confirmShoppingCartTestCase3() {
        UUID userId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();
        String jwtBearer = "Bearer token";

        ConfirmShoppingCartRequest request = new ConfirmShoppingCartRequest(deliveryAddressId);

        when(salesOrderClient.getDeliveryAddress(deliveryAddressId, jwtBearer))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId)).thenReturn(List.of());

        assertThrows(EmptyShoppingCartException.class, () ->
                shoppingCartService.confirmShoppingCart(request, userId, jwtBearer)
        );
    }

    @Test
    @DisplayName("Should confirm cart with multiple items")
    void confirmShoppingCartTestCase4() {
        UUID userId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();
        String jwtBearer = "Bearer token";

        ConfirmShoppingCartRequest request = new ConfirmShoppingCartRequest(deliveryAddressId);

        CreateOrderMessage.OrderItem orderItem1 = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(), "Product 1", 2, BigDecimal.valueOf(50)
        );
        CreateOrderMessage.OrderItem orderItem2 = new CreateOrderMessage.OrderItem(
                UUID.randomUUID(), "Product 2", 1, BigDecimal.valueOf(75)
        );

        ShoppingCartItemProductSKUSummary summary1 = new ShoppingCartItemProductSKUSummary();
        summary1.setProductSKUId(UUID.randomUUID());
        summary1.setProductSKUName("Product 1");
        summary1.setUnits(2);
        summary1.setCurrentPrice(BigDecimal.valueOf(50));

        ShoppingCartItemProductSKUSummary summary2 = new ShoppingCartItemProductSKUSummary();
        summary2.setProductSKUId(UUID.randomUUID());
        summary2.setProductSKUName("Product 2");
        summary2.setUnits(1);
        summary2.setCurrentPrice(BigDecimal.valueOf(75));

        when(salesOrderClient.getDeliveryAddress(deliveryAddressId, jwtBearer))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId))
                .thenReturn(List.of(summary1, summary2));
        when(shoppingCartItemProductSKUMapper.toCreateOrderMessageItem(summary1)).thenReturn(orderItem1);
        when(shoppingCartItemProductSKUMapper.toCreateOrderMessageItem(summary2)).thenReturn(orderItem2);

        shoppingCartService.confirmShoppingCart(request, userId, jwtBearer);

        verify(productStockManagementService, times(2)).reduceProductStock(any(), anyInt());
        verify(stockMovementService, times(2)).registerSale(any(), anyInt(), eq(userId));
        verify(shoppingCartConfirmationProducer).produce(any(CreateOrderMessage.class));
    }
}