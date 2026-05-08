package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.exception.ProductCategoryNotFoundException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.domain.entity.ProductCategory;
import com.products.infra.persistence.ProductCategoryRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductCategoryServiceTests {
    @Mock private ProductCategoryMapper productCategoryMapper;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private PagedResponseFactory<ProductCategoryResponse> pagedResponseFactory;

    @InjectMocks private ProductCategoryService productCategoryService;

    @Test
    @DisplayName("Should retrieve product category by ID successfully")
    void getByIdTestCase1() {
        // Given
        Integer categoryId = 1;

        ProductCategory category = new ProductCategory();
        category.setId(categoryId);
        category.setName("Electronics");

        ProductCategoryResponse response = new ProductCategoryResponse(1, "Electronics");

        when(productCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productCategoryMapper.toResponse(category)).thenReturn(response);

        // When
        ProductCategoryResponse result = productCategoryService.getById(categoryId);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.name());
        verify(productCategoryRepository).findById(categoryId);
        verify(productCategoryMapper).toResponse(category);
    }

    @Test
    @DisplayName("Should throw ProductCategoryNotFoundException when category not exists")
    void getByIdTestCase2() {
        // Given
        Integer categoryId = 999;

        when(productCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductCategoryNotFoundException.class, () -> {
            productCategoryService.getById(categoryId);
        });

        verify(productCategoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("Should retrieve all active ProductCategory successfully")
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

        PagedResponse<ProductCategoryResponse> expectedResponse = PagedResponse.<ProductCategoryResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productCategoryRepository.findAll(pageable)).thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductCategoryResponse> result = productCategoryService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.totalElements());
        assertEquals(2, result.content().size());
        verify(productCategoryRepository).findAll(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductCategory")
    void getAllCategoriesTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductCategory> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        PagedResponse<ProductCategoryResponse> expectedResponse = PagedResponse.<ProductCategoryResponse>builder()
                .content(new java.util.ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productCategoryRepository.findAll(pageable)).thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any())).thenReturn(expectedResponse);

        // When
        PagedResponse<ProductCategoryResponse> result = productCategoryService.getAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productCategoryRepository).findAll(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }
}