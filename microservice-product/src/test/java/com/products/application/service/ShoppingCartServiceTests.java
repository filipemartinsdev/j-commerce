package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.exception.ProductOutOfStockException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ShoppingCartItemNotFoundException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTests {

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

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Test
    @DisplayName("Should return paged shopping cart items successfully")
    void getAllItemsTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ShoppingCartItemProductSKUSummary entity = new ShoppingCartItemProductSKUSummary();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setProductSKUId(UUID.randomUUID());
        entity.setProductSKUName("Test SKU");
        entity.setOriginalPrice(BigDecimal.valueOf(100));
        entity.setCurrentPrice(BigDecimal.valueOf(80));
        entity.setPriceTypeName("PROMOTIONAL");
        entity.setUnits(2);

        Page<ShoppingCartItemProductSKUSummary> page = new PageImpl<>(List.of(entity), pageable, 1);

        ShoppingCartItemResponse response = new ShoppingCartItemResponse(
                entity.getId(),
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                entity.getUnits(),
                entity.getOriginalPrice(),
                entity.getCurrentPrice(),
                20
        );

        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId, pageable))
                .thenReturn(page);
        when(shoppingCartItemProductSKUMapper.toResponse(entity))
                .thenReturn(response);

        PagedResponse<ShoppingCartItemResponse> result = shoppingCartService.getAllItems(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(1, result.totalElements());
        assertTrue(result.isLast());
        assertEquals(1, result.content().size());

        verify(shoppingCartItemProductSKUSummaryRepository).findAllByUserId(userId, pageable);
        verify(shoppingCartItemProductSKUMapper).toResponse(entity);
    }

    @Test
    @DisplayName("Should return empty page when user has no shopping cart items")
    void getAllItemsTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<ShoppingCartItemProductSKUSummary> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<ShoppingCartItemResponse> result = shoppingCartService.getAllItems(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(0, result.totalPages());
        assertEquals(0, result.totalElements());
        assertTrue(result.isLast());
        assertTrue(result.content().isEmpty());

        verify(shoppingCartItemProductSKUSummaryRepository).findAllByUserId(userId, pageable);
        verify(shoppingCartItemProductSKUMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should create shopping cart item successfully when product SKU exists")
    void createItemTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 2);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);
        productSKU.setSKU("TEST-SKU");
        productSKU.setName("Test Product SKU");

        when(productSKURepository.findActiveById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(shoppingCartItemRepository.save(any(ShoppingCartItem.class)))
                .thenReturn(new ShoppingCartItem());
        when(productStockChecker.isTheProductWithStockEnough(any(), any()))
                .thenReturn(true);

        shoppingCartService.createItemByUserId(request, userId);

        verify(productSKURepository).findActiveById(productSKUId);
        verify(shoppingCartItemRepository).save(any(ShoppingCartItem.class));
        verify(productStockChecker).isTheProductWithStockEnough(any(), any());
    }

    @Test
    @DisplayName("Should throw ProductSKUNotFoundException when product SKU does not exist")
    void createItemTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 2);

        when(productSKURepository.findActiveById(productSKUId))
                .thenReturn(Optional.empty());

        assertThrows(ProductSKUNotFoundException.class, () ->
                shoppingCartService.createItemByUserId(request, userId));

        verify(productSKURepository).findActiveById(productSKUId);
        verify(shoppingCartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ProductOutOfStockException when product SKU is out of stock")
    void createItemTestCase3() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest(productSKUId, 2);

        var mockProductSKU = new ProductSKU();
        mockProductSKU.setId(productSKUId);

        when(productSKURepository.findActiveById(productSKUId))
                .thenReturn(Optional.of(mockProductSKU));
        when(productStockChecker.isTheProductWithStockEnough(any(), any()))
                .thenReturn(false);

        assertThrows(ProductOutOfStockException.class, () ->
                shoppingCartService.createItemByUserId(request, userId));

        verify(shoppingCartItemRepository, never()).save(any());
        verify(productStockChecker).isTheProductWithStockEnough(productSKUId, 2);
    }

    @Test
    @DisplayName("Should delete shopping cart item successfully when item exists and is active")
    void deleteItemByIdTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        ShoppingCartItem item = new ShoppingCartItem();
        item.setId(itemId);
        item.setUserId(userId);
        item.setIsActive(true);

        when(shoppingCartItemRepository.findActiveByIdAndUserId(itemId, userId))
                .thenReturn(Optional.of(item));
        when(shoppingCartItemRepository.save(any(ShoppingCartItem.class)))
                .thenReturn(item);

        shoppingCartService.deleteItemById(itemId, userId);

        verify(shoppingCartItemRepository).findActiveByIdAndUserId(itemId, userId);
        verify(shoppingCartItemRepository).save(item);

        assertFalse(item.getIsActive());
    }

    @Test
    @DisplayName("Should throw ShoppingCartItemNotFoundException when item does not exist")
    void deleteItemByIdTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(shoppingCartItemRepository.findActiveByIdAndUserId(itemId, userId))
                .thenReturn(Optional.empty());

        assertThrows(ShoppingCartItemNotFoundException.class, () ->
                shoppingCartService.deleteItemById(itemId, userId));

        verify(shoppingCartItemRepository).findActiveByIdAndUserId(itemId, userId);
        verify(shoppingCartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete all shopping cart items by user id")
    void deleteAllItemsByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();

        doNothing().when(shoppingCartItemRepository).markAllAsInactiveByUserId(userId);

        shoppingCartService.deleteAllItemsByUserId(userId);

        verify(shoppingCartItemRepository).markAllAsInactiveByUserId(userId);
    }
}