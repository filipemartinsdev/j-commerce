package com.products.application.service;

import com.products.application.dto.catalogue.CreateWishlistItemRequest;
import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.WishlistItemAlreadyExistsException;
import com.products.application.exception.WishlistItemNotFoundException;
import com.products.application.service.mapper.WishlistItemMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.WishlistItem;
import com.products.domain.entity.WishlistItemId;
import com.products.domain.entity.WishlistItemProductSKUResume;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.WishlistItemProductSKUResumeRepository;
import com.products.infra.persistence.WishlistItemRepository;
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
public class WishlistServiceTests {

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductSKURepository productSKURepository;

    @Mock
    private WishlistItemProductSKUResumeRepository wishlistItemProductSKUResumeRepository;

    @Mock
    private WishlistItemMapper wishlistItemMapper;

    @Mock
    private ProductDiscountCalculator productDiscountCalculator;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("Should return paged wishlist items successfully")
    void getAllItemsTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        WishlistItemProductSKUResume entity = new WishlistItemProductSKUResume();
        entity.setUserId(userId);
        entity.setProductSKUId(UUID.randomUUID());
        entity.setProductSKUName("Test SKU");
        entity.setOriginalPrice(BigDecimal.valueOf(100));
        entity.setCurrentPrice(BigDecimal.valueOf(80));
        entity.setPriceTypeName("PROMOTIONAL");

        Page<WishlistItemProductSKUResume> page = new PageImpl<>(List.of(entity), pageable, 1);

        WishlistItemResponse response = new WishlistItemResponse(
                entity.getProductSKUId(),
                entity.getProductSKUName(),
                null
        );

        when(wishlistItemProductSKUResumeRepository.findAllByUserId(userId, pageable))
                .thenReturn(page);
        when(productDiscountCalculator.getDiscountPercent(entity.getOriginalPrice(), entity.getCurrentPrice()))
                .thenReturn(20);
        when(wishlistItemMapper.toResponse(entity, 20))
                .thenReturn(response);

        PagedResponse<WishlistItemResponse> result = wishlistService.getAllItems(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(1, result.totalElements());
        assertTrue(result.isLast());
        assertEquals(1, result.content().size());

        verify(wishlistItemProductSKUResumeRepository).findAllByUserId(userId, pageable);
        verify(productDiscountCalculator).getDiscountPercent(entity.getOriginalPrice(), entity.getCurrentPrice());
        verify(wishlistItemMapper).toResponse(entity, 20);
    }

    @Test
    @DisplayName("Should return empty page when user has no wishlist items")
    void getAllItemsTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<WishlistItemProductSKUResume> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(wishlistItemProductSKUResumeRepository.findAllByUserId(userId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<WishlistItemResponse> result = wishlistService.getAllItems(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(0, result.totalPages());
        assertEquals(0, result.totalElements());
        assertTrue(result.isLast());
        assertTrue(result.content().isEmpty());

        verify(wishlistItemProductSKUResumeRepository).findAllByUserId(userId, pageable);
        verify(productDiscountCalculator, never()).getDiscountPercent(any(), any());
        verify(wishlistItemMapper, never()).toResponse(any(), any());
    }

    @Test
    @DisplayName("Should create wishlist item successfully when product SKU exists and not already in wishlist")
    void createItemTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateWishlistItemRequest request = new CreateWishlistItemRequest(productSKUId, 1);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);
        productSKU.setSKU("TEST-SKU");
        productSKU.setName("Test Product SKU");

        WishlistItemId wishlistItemId = new WishlistItemId(userId, productSKU);

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(wishlistItemRepository.existsById(wishlistItemId))
                .thenReturn(false);
        when(wishlistItemRepository.save(any(WishlistItem.class)))
                .thenReturn(new WishlistItem());

        wishlistService.createItem(request, userId);

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository).existsById(wishlistItemId);
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("Should throw ProductSKUNotFoundException when product SKU does not exist")
    void createItemTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateWishlistItemRequest request = new CreateWishlistItemRequest(productSKUId, 1);

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.empty());

        assertThrows(ProductSKUNotFoundException.class, () ->
                wishlistService.createItem(request, userId));

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository, never()).existsById(any());
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw WishlistItemAlreadyExistsException when item already exists in wishlist")
    void createItemTestCase3() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        CreateWishlistItemRequest request = new CreateWishlistItemRequest(productSKUId, 1);

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);
        productSKU.setSKU("TEST-SKU");
        productSKU.setName("Test Product SKU");

        WishlistItemId wishlistItemId = new WishlistItemId(userId, productSKU);

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(wishlistItemRepository.existsById(wishlistItemId))
                .thenReturn(true);

        assertThrows(WishlistItemAlreadyExistsException.class, () ->
                wishlistService.createItem(request, userId));

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository).existsById(wishlistItemId);
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete wishlist item successfully when item exists and is active")
    void deleteItemTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductSKU(productSKU);
        item.setIsActive(true);

        WishlistItemId wishlistItemId = new WishlistItemId(userId, productSKU);

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(wishlistItemRepository.findById(wishlistItemId))
                .thenReturn(Optional.of(item));
        when(wishlistItemRepository.save(any(WishlistItem.class)))
                .thenReturn(item);

        wishlistService.deleteItem(productSKUId, userId);

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository).findById(wishlistItemId);
        verify(wishlistItemRepository).save(item);

        assertFalse(item.getIsActive());
    }

    @Test
    @DisplayName("Should throw ProductSKUNotFoundException when product SKU does not exist on delete")
    void deleteItemTestCase2() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.empty());

        assertThrows(ProductSKUNotFoundException.class, () ->
                wishlistService.deleteItem(productSKUId, userId));

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository, never()).findById(any());
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw WishlistItemNotFoundException when wishlist item does not exist")
    void deleteItemTestCase3() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        WishlistItemId wishlistItemId = new WishlistItemId(userId, productSKU);

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(wishlistItemRepository.findById(wishlistItemId))
                .thenReturn(Optional.empty());

        assertThrows(WishlistItemNotFoundException.class, () ->
                wishlistService.deleteItem(productSKUId, userId));

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository).findById(wishlistItemId);
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ProductSKUNotFoundException when wishlist item is already inactive")
    void deleteItemTestCase4() {
        UUID userId = UUID.randomUUID();
        UUID productSKUId = UUID.randomUUID();

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductSKU(productSKU);
        item.setIsActive(false);

        WishlistItemId wishlistItemId = new WishlistItemId(userId, productSKU);

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(wishlistItemRepository.findById(wishlistItemId))
                .thenReturn(Optional.of(item));

        assertThrows(ProductSKUNotFoundException.class, () ->
                wishlistService.deleteItem(productSKUId, userId));

        verify(productSKURepository).findById(productSKUId);
        verify(wishlistItemRepository).findById(wishlistItemId);
        verify(wishlistItemRepository, never()).save(any());
    }
}