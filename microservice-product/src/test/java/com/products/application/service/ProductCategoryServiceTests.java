package com.products.application.service;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.catalogue.CatalogueCategoriesCursor;
import com.products.application.exception.ProductCategoryNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.domain.entity.ProductCategory;
import com.products.infra.persistence.ProductCategoryRepository;
import io.github.responsekit.core.SlicedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.ArrayList;
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
    @Mock private CursorCodec cursorCodec;

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
    @DisplayName("Should retrieve all active ProductCategory successfully when opaque cursor is null")
    void getAllCategoriesTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductCategory category1 = new ProductCategory();
        category1.setId(1);
        category1.setName("Electronics");

        ProductCategory category2 = new ProductCategory();
        category2.setId(2);
        category2.setName("Clothing");

        Slice<ProductCategory> slice = new SliceImpl<>(List.of(category1, category2), pageable, false);

        ProductCategoryResponse response1 = new ProductCategoryResponse(1, "Electronics");
        ProductCategoryResponse response2 = new ProductCategoryResponse(2, "Clothing");

        SlicedResponse<ProductCategoryResponse> expectedResponse = SlicedResponse
                .content(List.of(response1, response2))
                .size(10)
                .isLast(true)
                .firstCursor("")
                .lastCursor("")
                .build();

        when(productCategoryRepository.findAllWithoutCursor(pageable)).thenReturn(slice);
        when(cursorCodec.encode(any())).thenReturn("");
        when(productCategoryMapper.toResponse(category1)).thenReturn(response1);
        when(productCategoryMapper.toResponse(category2)).thenReturn(response2);

        // When
        SlicedResponse<ProductCategoryResponse> result = productCategoryService.getAll(null, 10);

        // Then

        verify(productCategoryRepository).findAllWithoutCursor(pageable);
        assertEquals(expectedResponse, result);
    }

    @Test @DisplayName("Should retrieve empty SlicedResponse if not exists any active ProductCategory")
    void getAllCategoriesTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Slice<ProductCategory> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

        SlicedResponse<ProductCategoryResponse> expectedResponse = SlicedResponse
                .content(new ArrayList<ProductCategoryResponse>())
                .size(10)
                .isLast(true)
                .firstCursor(null)
                .lastCursor(null)
                .build();

        when(productCategoryRepository.findAllWithoutCursor(pageable)).thenReturn(emptySlice);

        // When
        SlicedResponse<ProductCategoryResponse> result = productCategoryService.getAll(null, 10);

        // Then
        verify(productCategoryRepository).findAllWithoutCursor(pageable);
        assertEquals(expectedResponse, result);
    }
}