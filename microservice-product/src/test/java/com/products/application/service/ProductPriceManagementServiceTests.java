package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.PriceTypeResponse;
import com.products.application.dto.admin.CreateProductSKUPrice;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.application.dto.admin.UpdateProductSKUPriceRequest;
import com.products.application.exception.InvalidProductPriceTypeException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductSKUPriceNotFoundException;
import com.products.application.exception.ProductSKUWithoutBasePriceException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.service.mapper.ProductSKUPriceMapper;
import com.products.domain.entity.PriceType;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductSKUPrice;
import com.products.infra.persistence.PriceTypeRepository;
import com.products.infra.persistence.ProductSKUPriceRepository;
import com.products.infra.persistence.ProductSKURepository;
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
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPriceManagementServiceTests {
    @Mock
    private ProductSKUPriceRepository productSKUPriceRepository;

    @Mock
    private ProductSKUPriceMapper productSKUPriceMapper;

    @Mock
    private ProductSKURepository productSKURepository;

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @Mock
    private PagedResponseFactory<ProductSKUPriceResponse> pagedResponseFactory;

    @InjectMocks
    private ProductPriceManagementService productPriceManagementService;

    @Test @DisplayName("Should retrieve all active ProductSKUPrice successfully")
    void getAllPricesTestCase1() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        ProductSKUPrice price1 = new ProductSKUPrice();
        price1.setId(UUID.randomUUID());
        price1.setPrice(new BigDecimal("100.00"));
        price1.setIsActive(true);

        ProductSKUPrice price2 = new ProductSKUPrice();
        price2.setId(UUID.randomUUID());
        price2.setPrice(new BigDecimal("150.00"));
        price2.setIsActive(true);

        Page<ProductSKUPrice> page = new PageImpl<>(List.of(price1, price2), pageable, 2);

        ProductSKUPriceResponse response1 = new ProductSKUPriceResponse(
                price1.getId(), UUID.randomUUID(), "SKU1", new BigDecimal("100.00"),
                new PriceTypeResponse(1, "Base"), Instant.now(), null, Instant.now()
        );
        ProductSKUPriceResponse response2 = new ProductSKUPriceResponse(
                price2.getId(), UUID.randomUUID(), "SKU2", new BigDecimal("150.00"),
                new PriceTypeResponse(2, "Discount"), Instant.now(), null, Instant.now()
        );

        PagedResponse<ProductSKUPriceResponse> expectedResponse = PagedResponse.<ProductSKUPriceResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productSKUPriceRepository.findAllActive(pageable))
                .thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSKUPriceResponse> result = productPriceManagementService.getAllPrices(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.isLast());
        assertEquals(2, result.content().size());
        verify(productSKUPriceRepository).findAllActive(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductSKUPrice")
    void getAllPricesTestCase2() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductSKUPrice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        PagedResponse<ProductSKUPriceResponse> expectedResponse = PagedResponse.<ProductSKUPriceResponse>builder()
                .content(new java.util.ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productSKUPriceRepository.findAllActive(pageable))
                .thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSKUPriceResponse> result = productPriceManagementService.getAllPrices(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productSKUPriceRepository).findAllActive(pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should retrieve all active ProductSKUPrice for ProductSKU successfully")
    void getAllPricesByProductSKUIdTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        ProductSKUPrice price1 = new ProductSKUPrice();
        price1.setId(UUID.randomUUID());
        price1.setPrice(new BigDecimal("100.00"));
        price1.setIsActive(true);

        ProductSKUPrice price2 = new ProductSKUPrice();
        price2.setId(UUID.randomUUID());
        price2.setPrice(new BigDecimal("120.00"));
        price2.setIsActive(true);

        Page<ProductSKUPrice> page = new PageImpl<>(List.of(price1, price2), pageable, 2);

        ProductSKUPriceResponse response1 = new ProductSKUPriceResponse(
                price1.getId(), productSKUId, "SKU1", new BigDecimal("100.00"),
                new PriceTypeResponse(1, "Base"), Instant.now(), null, Instant.now()
        );
        ProductSKUPriceResponse response2 = new ProductSKUPriceResponse(
                price2.getId(), productSKUId, "SKU1", new BigDecimal("120.00"),
                new PriceTypeResponse(2, "Premium"), Instant.now(), null, Instant.now()
        );

        PagedResponse<ProductSKUPriceResponse> expectedResponse = PagedResponse.<ProductSKUPriceResponse>builder()
                .page(0)
                .size(10)
                .isLast(true)
                .totalElements(2L)
                .totalPages(1)
                .content(List.of(response1, response2))
                .build();

        when(productSKUPriceRepository.findAllActiveByProductSKUId(productSKUId, pageable))
                .thenReturn(page);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSKUPriceResponse> result = productPriceManagementService.getAllPricesByProductSKUId(productSKUId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());
        verify(productSKUPriceRepository).findAllActiveByProductSKUId(productSKUId, pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should throw ProductSKUNotFoundException if ProductSKU not exists by ID")
    void getAllPricesByProductSKUIdTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(productSKUPriceRepository.findAllActiveByProductSKUId(productSKUId, pageable))
                .thenThrow(new ProductSKUNotFoundException("ProductSKU not found"));

        // When & Then
        assertThrows(ProductSKUNotFoundException.class, () -> {
            productPriceManagementService.getAllPricesByProductSKUId(productSKUId, pageable);
        });

        verify(productSKUPriceRepository).findAllActiveByProductSKUId(productSKUId, pageable);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any active ProductSKUPrice by ProductSKU ID")
    void getAllPricesByProductSKUIdTestCase3() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductSKUPrice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        PagedResponse<ProductSKUPriceResponse> expectedResponse = PagedResponse.<ProductSKUPriceResponse>builder()
                .content(new java.util.ArrayList<>())
                .size(10)
                .page(0)
                .isLast(true)
                .totalPages(0)
                .totalElements(0L)
                .build();

        when(productSKUPriceRepository.findAllActiveByProductSKUId(productSKUId, pageable))
                .thenReturn(emptyPage);
        when(pagedResponseFactory.fromPage(any(), any()))
                .thenReturn(expectedResponse);

        // When
        PagedResponse<ProductSKUPriceResponse> result = productPriceManagementService.getAllPricesByProductSKUId(productSKUId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(productSKUPriceRepository).findAllActiveByProductSKUId(productSKUId, pageable);
        verify(pagedResponseFactory).fromPage(any(), any());
    }

    @Test @DisplayName("Should create ProductSKUPrice successfully if everything is OK")
    void createTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Integer priceTypeId = 1;

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);
        productSKU.setName("SKU1");

        PriceType priceType = new PriceType();
        priceType.setId(priceTypeId);
        priceType.setName("Base");

        ProductSKUPrice savedPrice = new ProductSKUPrice();
        savedPrice.setId(UUID.randomUUID());
        savedPrice.setProductSKU(productSKU);
        savedPrice.setPrice(new BigDecimal("100.00"));
        savedPrice.setPriceType(priceType);
        savedPrice.setStartAt(Instant.now());
        savedPrice.setIsActive(true);

        ProductSKUPriceResponse response = new ProductSKUPriceResponse(
                savedPrice.getId(), productSKUId, "SKU1", new BigDecimal("100.00"),
                new PriceTypeResponse(priceTypeId, "Base"), Instant.now(), null, Instant.now()
        );

        CreateProductSKUPrice request = new CreateProductSKUPrice(
                productSKUId, new BigDecimal("100.00"), priceTypeId, Optional.empty(), Optional.empty()
        );

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(priceTypeRepository.findById(priceTypeId))
                .thenReturn(Optional.of(priceType));
        when(productSKUPriceRepository.save(any(ProductSKUPrice.class)))
                .thenReturn(savedPrice);
        when(productSKUPriceMapper.toResponse(savedPrice))
                .thenReturn(response);

        // When
        ProductSKUPriceResponse result = productPriceManagementService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.price());
        assertEquals("SKU1", result.productSKUName());
        verify(productSKURepository).findById(productSKUId);
        verify(priceTypeRepository).findById(priceTypeId);
        verify(productSKUPriceRepository).save(any(ProductSKUPrice.class));
        verify(productSKUPriceMapper).toResponse(savedPrice);
    }

    @Test @DisplayName("Should throw ProductSKUNotFoundException if ProductSKU not exists")
    void createTestCase2() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Integer priceTypeId = 1;
        PriceType priceType = new PriceType(1, "common");

        CreateProductSKUPrice request = new CreateProductSKUPrice(
                productSKUId, new BigDecimal("100.00"), priceTypeId, Optional.empty(), Optional.empty()
        );

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUNotFoundException.class, () -> {
            productPriceManagementService.create(request);
        });

        verify(productSKURepository).findById(productSKUId);
        verify(priceTypeRepository, never()).findById(anyInt());
        verify(productSKUPriceRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw InvalidProductPriceTypeException if PriceType not exists by ID")
    void createTestCase3() {
        // Given
        UUID productSKUId = UUID.randomUUID();
        Integer priceTypeId = 999;

        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        CreateProductSKUPrice request = new CreateProductSKUPrice(
                productSKUId, new BigDecimal("100.00"), priceTypeId, Optional.empty(), Optional.empty()
        );

        ProductSKUPrice basePrice = new ProductSKUPrice();


        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(priceTypeRepository.findById(priceTypeId))
                .thenReturn(Optional.empty());
        when(productSKUPriceRepository.findAllActiveBasePriceByProductSKUId(productSKUId))
                .thenReturn(List.of(basePrice));

        // When & Then
        assertThrows(InvalidProductPriceTypeException.class, () -> {
            productPriceManagementService.create(request);
        });

        verify(productSKURepository).findById(productSKUId);
        verify(priceTypeRepository).findById(priceTypeId);
        verify(productSKUPriceRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw ProductSKUWithoutBasePriceException if the product haven't a base price yet")
    void createTestCase4(){
        // Given
        UUID productSKUId = UUID.randomUUID();
        ProductSKU productSKU = new ProductSKU();
        productSKU.setId(productSKUId);

        CreateProductSKUPrice request = new CreateProductSKUPrice(
                productSKUId, new BigDecimal("100.00"), 2, Optional.empty(), Optional.empty()
        );

        when(productSKURepository.findById(productSKUId))
                .thenReturn(Optional.of(productSKU));
        when(productSKUPriceRepository.findAllActiveBasePriceByProductSKUId(productSKUId))
                .thenReturn(List.of());

        // When & Then
        assertThrows(ProductSKUWithoutBasePriceException.class, () -> {
            productPriceManagementService.create(request);
        });

        verify(productSKURepository).findById(productSKUId);
        verify(productSKUPriceRepository, never()).save(any());
    }

    @Test @DisplayName("Should mark ProductSKUPrice as inactive successfully")
    void deleteByIdTestCase1() {
        // Given
        UUID priceId = UUID.randomUUID();

        ProductSKUPrice price = new ProductSKUPrice();
        price.setId(priceId);
        price.setPrice(new BigDecimal("100.00"));
        price.setIsActive(true);

        ProductSKUPrice inactivePrice = new ProductSKUPrice();
        inactivePrice.setId(priceId);
        inactivePrice.setPrice(new BigDecimal("100.00"));
        inactivePrice.setIsActive(false);

        when(productSKUPriceRepository.findById(priceId))
                .thenReturn(Optional.of(price));
        when(productSKUPriceRepository.save(any(ProductSKUPrice.class)))
                .thenReturn(inactivePrice);

        // When
        productPriceManagementService.deleteById(priceId);

        // Then
        verify(productSKUPriceRepository).findById(priceId);
        verify(productSKUPriceRepository).save(any(ProductSKUPrice.class));
    }

    @Test @DisplayName("Should throw ProductSKUPriceNotFoundException if ProductSKUPrice is inactive or not exists")
    void deleteByIdTestCase2() {
        // Given
        UUID priceId = UUID.randomUUID();

        when(productSKUPriceRepository.findById(priceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUPriceNotFoundException.class, () -> {
            productPriceManagementService.deleteById(priceId);
        });

        verify(productSKUPriceRepository).findById(priceId);
        verify(productSKUPriceRepository, never()).save(any());
    }

    @Test @DisplayName("Should update ProductSKUPrice successfully if everything is OK")
    void updateTestCase1() {
        // Given
        UUID priceId = UUID.randomUUID();
        Integer priceTypeId = 2;

        PriceType priceType = new PriceType();
        priceType.setId(priceTypeId);
        priceType.setName("Premium");

        ProductSKUPrice existingPrice = new ProductSKUPrice();
        existingPrice.setId(priceId);
        existingPrice.setPrice(new BigDecimal("100.00"));
        existingPrice.setIsActive(true);

        ProductSKUPrice updatedPrice = new ProductSKUPrice();
        updatedPrice.setId(priceId);
        updatedPrice.setPrice(new BigDecimal("150.00"));
        updatedPrice.setPriceType(priceType);
        updatedPrice.setIsActive(true);

        ProductSKUPriceResponse response = new ProductSKUPriceResponse(
                priceId, UUID.randomUUID(), "SKU1", new BigDecimal("150.00"),
                new PriceTypeResponse(priceTypeId, "Premium"), Instant.now(), null, Instant.now()
        );

        UpdateProductSKUPriceRequest request = new UpdateProductSKUPriceRequest(
                Optional.of(new BigDecimal("150.00")), Optional.of(priceTypeId),
                Optional.empty(), Optional.empty()
        );

        when(productSKUPriceRepository.findById(priceId))
                .thenReturn(Optional.of(existingPrice));
        when(priceTypeRepository.getReferenceById(priceTypeId))
                .thenReturn(priceType);
        when(productSKUPriceRepository.save(any(ProductSKUPrice.class)))
                .thenReturn(updatedPrice);
        when(productSKUPriceMapper.toResponse(updatedPrice))
                .thenReturn(response);

        // When
        ProductSKUPriceResponse result = productPriceManagementService.update(priceId, request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.price());
        verify(productSKUPriceRepository).findById(priceId);
        verify(priceTypeRepository).getReferenceById(priceTypeId);
        verify(productSKUPriceRepository).save(any(ProductSKUPrice.class));
        verify(productSKUPriceMapper).toResponse(updatedPrice);
    }

    @Test @DisplayName("Should throw ProductSKUPriceNotFoundException if ProductSKUPrice is inactive or not exists by ID")
    void updateTestCase2() {
        // Given
        UUID priceId = UUID.randomUUID();

        UpdateProductSKUPriceRequest request = new UpdateProductSKUPriceRequest(
                Optional.of(new BigDecimal("150.00")), Optional.empty(),
                Optional.empty(), Optional.empty()
        );

        when(productSKUPriceRepository.findById(priceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductSKUPriceNotFoundException.class, () -> {
            productPriceManagementService.update(priceId, request);
        });

        verify(productSKUPriceRepository).findById(priceId);
        verify(productSKUPriceRepository, never()).save(any());
    }

    @Test @DisplayName("Should throw InvalidProductPriceTypeException if PriceType not exists by ID")
    void updateTestCase3() {
        // Given
        UUID priceId = UUID.randomUUID();
        Integer invalidPriceTypeId = 999;

        ProductSKUPrice existingPrice = new ProductSKUPrice();
        existingPrice.setId(priceId);
        existingPrice.setPrice(new BigDecimal("100.00"));
        existingPrice.setIsActive(true);

        UpdateProductSKUPriceRequest request = new UpdateProductSKUPriceRequest(
                Optional.empty(), Optional.of(invalidPriceTypeId),
                Optional.empty(), Optional.empty()
        );

        when(productSKUPriceRepository.findById(priceId))
                .thenReturn(Optional.of(existingPrice));
        when(priceTypeRepository.getReferenceById(invalidPriceTypeId))
                .thenThrow(new InvalidProductPriceTypeException("Invalid PriceType"));

        // When & Then
        assertThrows(InvalidProductPriceTypeException.class, () -> {
            productPriceManagementService.update(priceId, request);
        });

        verify(productSKUPriceRepository).findById(priceId);
        verify(priceTypeRepository).getReferenceById(invalidPriceTypeId);
        verify(productSKUPriceRepository, never()).save(any());
    }

    @Test @DisplayName("Should marks all ProductSKUPrice as inactive by productSKUId successfully")
    void deleteAllByProductIdTestCase1() {
        // Given
        UUID productSKUId = UUID.randomUUID();

        // When
        productPriceManagementService.deleteAllByProductSKUId(productSKUId);

        // Then
        verify(productSKUPriceRepository).setInactiveAllByProductSKUId(productSKUId);
    }
}