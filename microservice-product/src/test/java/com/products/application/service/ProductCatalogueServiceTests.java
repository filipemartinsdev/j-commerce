package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.StockStatus;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductResumeCatalogueResponse;
import com.products.application.dto.catalogue.ProductSKUSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.exception.InvalidProductCategoryException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUSummaryCatalogueMapper;
import com.products.domain.entity.*;
import com.products.infra.persistence.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCatalogueServiceTests {
    @Mock private ProductResumeCatalogueRepository productCatalogueResumeRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductCategoryMapper productCategoryMapper;
    @Mock private ProductRepository productRepository;
    @Mock private ProductSKUSummaryCatalogueRepository productSKUSummaryCatalogueRepository;
    @Mock private ProductSKUSummaryCatalogueMapper productSKUSummaryCatalogueMapper;
    @Mock private ProductDiscountCalculator productDiscountCalculator;

    @InjectMocks
    private ProductCatalogueService productCatalogueService;

    @Test @DisplayName("Should retrieve all active ProductResumeCatalogue successfully")
    void getAllTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductResumeCatalogue product1 = new ProductResumeCatalogue();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setCategoryId(1);
        product1.setCategoryName("Electronics");
        product1.setOriginalPriceValue(new BigDecimal("100.00"));
        product1.setCurrentPriceValue(new BigDecimal("50.00"));
        product1.setCurrentPriceTypeName("Sale");
        product1.setStockCount(50);

        ProductResumeCatalogue product2 = new ProductResumeCatalogue();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setCategoryId(2);
        product2.setCategoryName("Clothing");
        product2.setOriginalPriceValue(new BigDecimal("50.00"));
        product2.setCurrentPriceValue(new BigDecimal("25.00"));
        product2.setCurrentPriceTypeName("Discount");
        product2.setStockCount(30);

        Page<ProductResumeCatalogue> page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(productCatalogueResumeRepository.findAll(pageable)).thenReturn(page);
        when(productDiscountCalculator.getDiscountPercent(any(), any())).thenReturn(50);

        // When
        PagedResponse<ProductResumeCatalogueResponse> result = productCatalogueService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productCatalogueResumeRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any ProductResumeCatalogue")
    void getAllTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResumeCatalogue> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productCatalogueResumeRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<ProductResumeCatalogueResponse> result = productCatalogueService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productCatalogueResumeRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve all active ProductResumeCatalogue by categoryId")
    void getAllByCategoryIdTestCase1() {
        // Given
        Integer categoryId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        ProductResumeCatalogue product1 = new ProductResumeCatalogue();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setCategoryId(categoryId);
        product1.setCategoryName("Electronics");
        product1.setOriginalPriceValue(new BigDecimal("100.00"));
        product1.setCurrentPriceValue(new BigDecimal("100.00"));
        product1.setCurrentPriceTypeName("Offer");

        Page<ProductResumeCatalogue> page = new PageImpl<>(List.of(product1), pageable, 1);

        when(productCategoryRepository.existsById(categoryId)).thenReturn(true);
        when(productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(page);
        when(productDiscountCalculator.getDiscountPercent(any(), any())).thenReturn(0);

        // When
        PagedResponse<ProductResumeCatalogueResponse> result = productCatalogueService.getAllByCategoryId(categoryId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        verify(productCategoryRepository).existsById(categoryId);
        verify(productCatalogueResumeRepository).findAllByCategoryId(categoryId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any ProductResumeCatalogue by categoryId")
    void getAllByCategoryIdTestCase2() {
        // Given
        Integer categoryId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResumeCatalogue> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productCategoryRepository.existsById(categoryId)).thenReturn(true);
        when(productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<ProductResumeCatalogueResponse> result = productCatalogueService.getAllByCategoryId(categoryId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productCategoryRepository).existsById(categoryId);
        verify(productCatalogueResumeRepository).findAllByCategoryId(categoryId, pageable);
    }

    @Test @DisplayName("Should throw InvalidProductCategoryException if ProductCategory not exists by ID")
    void getAllByCategoryIdTestCase3() {
        // Given
        Integer invalidCategoryId = 999;
        Pageable pageable = PageRequest.of(0, 10);

        when(productCategoryRepository.existsById(invalidCategoryId)).thenReturn(false);

        // When & Then
        assertThrows(InvalidProductCategoryException.class, () -> {
            productCatalogueService.getAllByCategoryId(invalidCategoryId, pageable);
        });

        verify(productCategoryRepository).existsById(invalidCategoryId);
        verify(productCatalogueResumeRepository, never()).findAllByCategoryId(anyInt(), any());
    }

    @Test @DisplayName("Should retrieve all active ProductCategory successfully")
    void getAllCategoriesTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductCategory category1 = new ProductCategory();
        category1.setId(1);
        category1.setName("Electronics");

        ProductCategory category2 = new ProductCategory();
        category2.setId(2);
        category2.setName("Clothing");

        Page<ProductCategory> page = new PageImpl<>(List.of(category1, category2), pageable, 2);

        ProductCategoryResponse response1 = new ProductCategoryResponse(1, "Electronics");
        ProductCategoryResponse response2 = new ProductCategoryResponse(2, "Clothing");

        when(productCategoryRepository.findAll(pageable)).thenReturn(page);
        when(productCategoryMapper.toResponse(category1)).thenReturn(response1);
        when(productCategoryMapper.toResponse(category2)).thenReturn(response2);

        // When
        PagedResponse<ProductCategoryResponse> result = productCatalogueService.getAllCategories(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.totalElements());
        assertEquals(2, result.content().size());
        verify(productCategoryRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductCategory")
    void getAllCategoriesTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductCategory> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productCategoryRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        PagedResponse<ProductCategoryResponse> result = productCatalogueService.getAllCategories(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productCategoryRepository).findAll(pageable);
    }

    @Test @DisplayName("Should retrieve ProductSummary by productSKUId successfully")
    void getProductSummaryByProductIdTestCase1() {
        // Given
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setName("Product 1");
        product.setDescription("Test Product");

        ProductCategory category = new ProductCategory();
        category.setId(1);
        category.setName("Electronics");
        product.setCategory(category);

        ProductSKUSummaryCatalogue sku1 = new ProductSKUSummaryCatalogue();
        sku1.setId(UUID.randomUUID());
        sku1.setProductId(productId);
        sku1.setSKU("SKU1");
        sku1.setName("Product SKU 1");
        sku1.setOriginalPrice(new BigDecimal("100.00"));
        sku1.setCurrentPrice(new BigDecimal("50.00"));

        ProductPriceCatalogueResponse priceResponse = new ProductPriceCatalogueResponse(
                new BigDecimal("100.00"), new BigDecimal("50.00"), 20, "Sale"
        );

        ProductSKUSummaryCatalogueResponse skuResponse = new ProductSKUSummaryCatalogueResponse(
                sku1.getId(), "SKU1", "Product SKU 1", StockStatus.IN_STOCK, priceResponse
        );

        ProductCategoryResponse categoryResponse = new ProductCategoryResponse(1, "Electronics");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productSKUSummaryCatalogueRepository.findAllByProductId(productId)).thenReturn(List.of(sku1));
        when(productCategoryMapper.toResponse(category)).thenReturn(categoryResponse);
        when(productDiscountCalculator.getDiscountPercent(any(), any())).thenReturn(50);
        when(productSKUSummaryCatalogueMapper.toResponse(sku1, 50)).thenReturn(skuResponse);

        // When
        ProductSummaryCatalogueResponse result = productCatalogueService.getProductSummaryByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.id());
        assertEquals("Product 1", result.name());
        assertEquals("Test Product", result.description());
        assertEquals(1, result.SKUs().size());
        verify(productRepository).findById(productId);
        verify(productSKUSummaryCatalogueRepository).findAllByProductId(productId);
    }

    @Test @DisplayName("Should throw ProductNotFoundException if product is not active or not exists by ID")
    void getProductSummaryByProductIdTestCase2() {
        // Given
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productCatalogueService.getProductSummaryByProductId(productId);
        });

        verify(productRepository).findById(productId);
        verify(productSKUSummaryCatalogueRepository, never()).findAllByProductId(any());
    }
}