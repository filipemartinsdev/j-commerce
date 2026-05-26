package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.StockStatus;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductSKUCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUCatalogueMapper;
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
    @Mock private ProductCatalogueViewRepository productCatalogueResumeRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductCategoryMapper productCategoryMapper;
    @Mock private ProductRepository productRepository;
    @Mock private ProductCatalogueSummaryViewRepository productCatalogueSummaryViewRepository;
    @Mock private ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper;
    @Mock private ProductDiscountCalculator productDiscountCalculator;
    @Mock private PagedResponseFactory<ProductSummaryCatalogueResponse> pagedResponseFactory;

    @InjectMocks
    private ProductCatalogueService productCatalogueService;

    @Test @DisplayName("Should retrieve all active ProductCatalogueView successfully")
    void getAllTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductCatalogueView product1 = new ProductCatalogueView();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setCategoryId(1);
        product1.setCategoryName("Electronics");
        product1.setOriginalPriceValue(new BigDecimal("100.00"));
        product1.setCurrentPriceValue(new BigDecimal("50.00"));
        product1.setCurrentPriceTypeName("Sale");
        product1.setStockCount(50);

        ProductCatalogueView product2 = new ProductCatalogueView();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setCategoryId(2);
        product2.setCategoryName("Clothing");
        product2.setOriginalPriceValue(new BigDecimal("50.00"));
        product2.setCurrentPriceValue(new BigDecimal("25.00"));
        product2.setCurrentPriceTypeName("Discount");
        product2.setStockCount(30);

        Page<ProductCatalogueView> page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        ProductPriceCatalogueResponse price1 = new ProductPriceCatalogueResponse(
                new BigDecimal("100.00"), new BigDecimal("50.00"), 50, "Sale"
        );
        ProductPriceCatalogueResponse price2 = new ProductPriceCatalogueResponse(
                new BigDecimal("50.00"), new BigDecimal("25.00"), 50, "Discount"
        );

        ProductSummaryCatalogueResponse response1 = new ProductSummaryCatalogueResponse(
                product1.getProductId(), "Product 1", new ProductCategoryResponse(1, "Electronics"), price1
        );
        ProductSummaryCatalogueResponse response2 = new ProductSummaryCatalogueResponse(
                product2.getProductId(), "Product 2", new ProductCategoryResponse(2, "Clothing"), price2
        );

        PagedResponse<ProductSummaryCatalogueResponse> expectedResponse = PagedResponse.<ProductSummaryCatalogueResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productCatalogueResumeRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productCatalogueResumeRepository).findAll(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any ProductCatalogueView")
    void getAllTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductCatalogueView> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        PagedResponse<ProductSummaryCatalogueResponse> expectedResponse = PagedResponse.<ProductSummaryCatalogueResponse>builder()
                .content(new java.util.ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productCatalogueResumeRepository.findAll(pageable)).thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productCatalogueResumeRepository).findAll(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve all active ProductCatalogueView by categoryId")
    void getAllByCategoryIdTestCase1() {
        // Given
        Integer categoryId = 1;
        Pageable pageable = PageRequest.of(0, 10);

        ProductCatalogueView product1 = new ProductCatalogueView();
        product1.setProductId(UUID.randomUUID());
        product1.setName("Product 1");
        product1.setCategoryId(categoryId);
        product1.setCategoryName("Electronics");
        product1.setOriginalPriceValue(new BigDecimal("100.00"));
        product1.setCurrentPriceValue(new BigDecimal("100.00"));
        product1.setCurrentPriceTypeName("Offer");

        Page<ProductCatalogueView> page = new PageImpl<>(List.of(product1), pageable, 1);

        ProductPriceCatalogueResponse price1 = new ProductPriceCatalogueResponse(
                new BigDecimal("100.00"), new BigDecimal("100.00"), 0, "Offer"
        );

        ProductSummaryCatalogueResponse response1 = new ProductSummaryCatalogueResponse(
                product1.getProductId(), "Product 1", new ProductCategoryResponse(1, "Electronics"), price1
        );

        PagedResponse<ProductSummaryCatalogueResponse> expectedResponse = PagedResponse.<ProductSummaryCatalogueResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(1L)
                .totalPages(1)
                .content(List.of(response1))
                .build();

        when(productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAllByCategoryId(categoryId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        verify(productCatalogueResumeRepository).findAllByCategoryId(categoryId, pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any ProductCatalogueView by categoryId")
    void getAllByCategoryIdTestCase2() {
        // Given
        Integer categoryId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductCatalogueView> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        PagedResponse<ProductSummaryCatalogueResponse> expectedResponse = PagedResponse.<ProductSummaryCatalogueResponse>builder()
                .content(new java.util.ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAllByCategoryId(categoryId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productCatalogueResumeRepository).findAllByCategoryId(categoryId, pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
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

        ProductCatalogueSummaryView sku1 = new ProductCatalogueSummaryView();
        sku1.setId(UUID.randomUUID());
        sku1.setProductId(productId);
        sku1.setSKU("SKU1");
        sku1.setName("Product SKU 1");
        sku1.setOriginalPrice(new BigDecimal("100.00"));
        sku1.setCurrentPrice(new BigDecimal("50.00"));

        ProductPriceCatalogueResponse priceResponse = new ProductPriceCatalogueResponse(
                new BigDecimal("100.00"), new BigDecimal("50.00"), 20, "Sale"
        );

        ProductSKUCatalogueResponse skuResponse = new ProductSKUCatalogueResponse(
                sku1.getId(), "SKU1", "Product SKU 1", StockStatus.IN_STOCK, priceResponse
        );

        ProductCategoryResponse categoryResponse = new ProductCategoryResponse(1, "Electronics");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productCatalogueSummaryViewRepository.findAllByProductId(productId)).thenReturn(List.of(sku1));
        when(productCategoryMapper.toResponse(category)).thenReturn(categoryResponse);
        when(productDiscountCalculator.getDiscountPercent(any(), any())).thenReturn(50);
        when(productSKUSummaryCatalogueMapper.toResponse(sku1, 50)).thenReturn(skuResponse);

        // When
        ProductCatalogueResponse result = productCatalogueService.getProductSummaryByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.id());
        assertEquals("Product 1", result.name());
        assertEquals("Test Product", result.description());
        assertEquals(1, result.SKUs().size());
        verify(productRepository).findById(productId);
        verify(productCatalogueSummaryViewRepository).findAllByProductId(productId);
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
        verify(productCatalogueSummaryViewRepository, never()).findAllByProductId(any());
    }
}