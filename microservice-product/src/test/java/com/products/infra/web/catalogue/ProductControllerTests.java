package com.products.infra.web.catalogue;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.StockStatus;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductSKUCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.ProductCatalogueService;
import com.products.application.service.ProductCategoryService;
import com.products.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
public class ProductControllerTests {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private ProductCatalogueService productCatalogueService;
    @MockitoBean private ProductCategoryService productCategoryService;

    @Test @DisplayName("Should retrieve product categories and status code 200")
    @WithMockUser(authorities = "SCOPE_USER")
    void getCategoriesTestCase1() throws Exception {
        ProductCategoryResponse productCategoryResponse = new ProductCategoryResponse(1, "testing");

        String expectedJSON = """
            {
                "status": "success",
                "data": {
                    "page": 0,
                    "size": 20,
                    "totalPages": 1,
                    "totalElements": 1,
                    "isLast": true,
                    "content": [
                        {
                            "id": 1,
                            "name": "testing"
                        }
                    ]
                }
            }
        """;

        when(productCategoryService.getAll(any()))
                .thenReturn(
                        PagedResponse.<ProductCategoryResponse>builder()
                                .page(0)
                                .size(20)
                                .totalElements(1L)
                                .totalPages(1)
                                .isLast(true)
                                .content(List.of(productCategoryResponse))
                                .build()
                );

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getCategoriesTestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should retrieve products and return status code 200")
    @WithMockUser(authorities = "SCOPE_USER")
    void getProductsTestCase1() throws Exception {
        var productSummaryCatalogueResponse = new ProductSummaryCatalogueResponse(
                UUID.randomUUID(),
                "testing",
                new ProductCategoryResponse(
                        1,
                        "testing"
                ),
                new ProductPriceCatalogueResponse(
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        0,
                        "testing"
                )
        );

        when(productCatalogueService.getAll(any())).thenReturn(
                PagedResponse.<ProductSummaryCatalogueResponse>builder()
                        .page(0)
                        .size(20)
                        .isLast(true)
                        .totalElements(1L)
                        .totalPages(1)
                        .content(List.of(productSummaryCatalogueResponse))
                        .build()
        );

        String expectedJSON = """
            {
                "status": "success",
                "data": {
                    "page": 0,
                    "size": 20,
                    "totalPages": 1,
                    "totalElements": 1,
                    "isLast": true,
                    "content": [
                        {
                            "productId": "%s",
                            "name": "testing",
                            "category": {
                                "id": 1,
                                "name": "testing"
                            },
                            "price": {
                                "original": 1.00,
                                "current": 1.00,
                                "discountPercent": 0,
                                "type": "testing"
                            }
                        }
                    ]
                }
            }
        """.formatted(productSummaryCatalogueResponse.productId());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getProductsTestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should retrieve product by ID and return status code 200")
    @WithMockUser(authorities = "SCOPE_USER")
    void getProductByIdTestCase1() throws Exception {
        ProductCatalogueResponse productCatalogueResponse = new ProductCatalogueResponse(
                UUID.randomUUID(),
                "testing",
                "testing",
                new ProductCategoryResponse(1, "testing"),
                List.of(
                        new ProductSKUCatalogueResponse(
                                UUID.randomUUID(),
                                "testing",
                                "testing",
                                StockStatus.IN_STOCK,
                                new ProductPriceCatalogueResponse(
                                        BigDecimal.ONE,
                                        BigDecimal.ONE,
                                        0,
                                        "testing"
                                )
                        )
                )
        );

        String expectedJSON = """
            {
                "status": "success",
                "data": {
                    "id": "%s",
                    "name": "testing",
                    "description": "testing",
                    "category": {
                        "id": 1,
                        "name": "testing"
                    },
                    "SKUs": [
                        {
                            "id": "%s",
                            "SKU": "testing",
                            "name": "testing",
                            "stockStatus": "IN_STOCK",
                            "price": {
                                "original": 1.00,
                                "current": 1.00,
                                "discountPercent": 0,
                                "type": "testing"
                            }
                        }
                    ]
                    }
            }
        """.formatted(productCatalogueResponse.id(),  productCatalogueResponse.SKUs().get(0).id());

        when(productCatalogueService.getProductSummaryByProductId(any()))
                .thenReturn(productCatalogueResponse);

        mockMvc.perform(get("/api/v1/products/"+productCatalogueResponse.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 404 if product not exists by ID")
    @WithMockUser(authorities = "SCOPE_USER")
    void getProductByIdTestCase2() throws Exception {
        var productId = UUID.randomUUID();

        doThrow(ProductNotFoundException.class)
                .when(productCatalogueService).getProductSummaryByProductId(productId);

        mockMvc.perform(get("/api/v1/products/"+productId))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getProductByIdTestCase3() throws Exception {
        mockMvc.perform(get("/api/v1/product/"+UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}