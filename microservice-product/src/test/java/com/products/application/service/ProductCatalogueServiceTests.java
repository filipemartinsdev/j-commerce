package com.products.application.service;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.StockStatus;
import com.products.application.dto.catalogue.*;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUCatalogueMapper;
import com.products.domain.entity.*;
import com.products.infra.persistence.*;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.SlicedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

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
    @Mock private CursorCodec cursorCodec;

    @InjectMocks
    private ProductCatalogueService productCatalogueService;

    @Test @DisplayName("Should retrieve all active ProductCatalogueView successfully when cursor is null")
    void getAllTestCase1() {
        // Given
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

        Pageable pageable = PageRequest.of(0, 10);
        Slice<ProductCatalogueView> slice = new SliceImpl<>(List.of(product1, product2), pageable, false);

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

        SlicedResponse<ProductSummaryCatalogueResponse> expectedResponse = SlicedResponse
                .content(List.of(response1, response2))
                .size(10)
                .isLast(true)
                .firstCursor("")
                .lastCursor("")
                .build();

        when(cursorCodec.encode(any())).thenReturn("");
        when(productCatalogueResumeRepository.findAllWithoutCursor(pageable)).thenReturn(slice);
        when(productDiscountCalculator.getDiscountPercent(any(), any())).thenReturn(50);

        // When
        SlicedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAll(null, 10);

        // Then
        verify(productCatalogueResumeRepository).findAllWithoutCursor(pageable);
        verify(cursorCodec, times(2)).encode(any());
        assertEquals(expectedResponse, result);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any ProductCatalogueView")
    void getAllTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ProductCatalogueView> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

        SlicedResponse<ProductSummaryCatalogueResponse> expectedResponse = SlicedResponse
                .content(new ArrayList<ProductSummaryCatalogueResponse>())
                .size(10)
                .isLast(true)
                .lastCursor(null)
                .firstCursor(null)
                .build();

        when(productCatalogueResumeRepository.findAllWithoutCursor(pageable)).thenReturn(emptySlice);

        // When
        SlicedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAll(null, 10);

        // Then
        assertEquals(expectedResponse, result);
        verify(productCatalogueResumeRepository).findAllWithoutCursor(pageable);
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

        Slice<ProductCatalogueView> slice = new SliceImpl<>(List.of(product1), pageable, false);

        ProductPriceCatalogueResponse price1 = new ProductPriceCatalogueResponse(
                new BigDecimal("100.00"), new BigDecimal("100.00"), 0, "Offer"
        );

        ProductSummaryCatalogueResponse response1 = new ProductSummaryCatalogueResponse(
                product1.getProductId(), "Product 1", new ProductCategoryResponse(1, "Electronics"), price1
        );

        SlicedResponse<ProductSummaryCatalogueResponse> expectedResponse = SlicedResponse
                .content(List.of(response1))
                .size(10)
                .isLast(true)
                .firstCursor("")
                .lastCursor("")
                .build();

        when(productCatalogueResumeRepository.findAllByCategoryWithoutCursor(categoryId, pageable)).thenReturn(slice);
        when(cursorCodec.encode(any())).thenReturn("");

        // When
        SlicedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAllByCategoryId(categoryId, null, 10);

        // Then
        assertEquals(expectedResponse, result);
        verify(productCatalogueResumeRepository).findAllByCategoryWithoutCursor(categoryId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any ProductCatalogueView by categoryId")
    void getAllByCategoryIdTestCase2() {
        // Given
        Integer categoryId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ProductCatalogueView> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

        SlicedResponse<ProductSummaryCatalogueResponse> expectedResponse = SlicedResponse
                .content(new ArrayList<ProductSummaryCatalogueResponse>())
                .size(10)
                .isLast(true)
                .firstCursor(null)
                .lastCursor(null)
                .build();

        when(productCatalogueResumeRepository.findAllByCategoryWithoutCursor(categoryId, pageable)).thenReturn(emptySlice);

        // When
        SlicedResponse<ProductSummaryCatalogueResponse> result = productCatalogueService.getAllByCategoryId(categoryId, null, 10);

        // Then
        assertEquals(expectedResponse, result);
        verify(productCatalogueResumeRepository).findAllByCategoryWithoutCursor(categoryId, pageable);
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