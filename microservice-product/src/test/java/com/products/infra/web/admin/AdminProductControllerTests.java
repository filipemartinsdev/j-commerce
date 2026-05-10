package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.admin.CreateProductRequest;
import com.products.application.dto.admin.ProductAdminResponse;
import com.products.application.exception.CantDeleteProductException;
import com.products.application.exception.InvalidProductCategoryException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.ProductManagementService;
import com.products.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProductController.class)
@Import(SecurityConfig.class)
public class AdminProductControllerTests {
    @MockitoBean private ProductManagementService adminProductService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve all products and return status code 200")
    void getAllProductsTestCase1() throws Exception {
        ProductAdminResponse productResponse = new ProductAdminResponse(
                UUID.randomUUID(),
                "testing",
                "testing description",
                new ProductCategoryResponse(1, "testing"),
                Instant.now(),
                null
        );

        var expectedResponse = PagedResponse.<ProductAdminResponse>builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .content(List.of(productResponse))
                .build();

        when(adminProductService.getAllProducts(any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve products by category ID and return status code 200")
    void getAllProductsTestCase2() throws Exception {
        ProductAdminResponse productResponse = new ProductAdminResponse(
                UUID.randomUUID(),
                "testing",
                "testing description",
                new ProductCategoryResponse(1, "testing"),
                Instant.now(),
                null
        );

        var expectedResponse = PagedResponse.<ProductAdminResponse>builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .content(List.of(productResponse))
                .build();

        when(adminProductService.getAllProductsByCategoryId(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/products")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllProductsTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void getAllProductsTestCase4() throws Exception {
        mockMvc.perform(get("/admin/api/v1/products"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve product by ID and return status code 200")
    void getProductByIdTestCase1() throws Exception {
        ProductAdminResponse productResponse = new ProductAdminResponse(
                UUID.randomUUID(),
                "testing",
                "testing description",
                new ProductCategoryResponse(1, "testing"),
                Instant.now(),
                null
        );

        when(adminProductService.getProductById(any())).thenReturn(productResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", productResponse
        ));

        mockMvc.perform(get("/admin/api/v1/products/" + productResponse.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product not exists")
    void getProductByIdTestCase2() throws Exception {
        when(adminProductService.getProductById(any()))
                .thenThrow(ProductNotFoundException.class);

        mockMvc.perform(get("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getProductByIdTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should create product and return status code 201")
    void createTestCase1() throws Exception {
        ProductAdminResponse productResponse = new ProductAdminResponse(
                UUID.randomUUID(),
                "testing",
                null,
                new ProductCategoryResponse(1, "testing"),
                Instant.now(),
                null
        );

        String requestBody = """
            {
                "name": "testing",
                "description": null,
                "categoryId": 1
            }
        """;

        when(adminProductService.createProduct(any())).thenReturn(productResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", productResponse
        ));

        mockMvc.perform(post("/admin/api/v1/products")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated())
        .andExpect(content().json(expectedJSON));

        verify(adminProductService).createProduct(any(CreateProductRequest.class));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void createTestCase2() throws Exception {
        String requestBody = """
            {
                "name": "testing",
                "categoryId": 1
            }
        """;

        mockMvc.perform(post("/admin/api/v1/products")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void createTestCase3() throws Exception {
        String requestBody = """
            {
                "name": "testing",
                "categoryId": 1
            }
        """;

        mockMvc.perform(post("/admin/api/v1/products")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 400 if category not exists")
    void createTestCase4() throws Exception {
        String requestBody = """
            {
                "name": "testing",
                "categoryId": 999
            }
        """;

        when(adminProductService.createProduct(any()))
                .thenThrow(InvalidProductCategoryException.class);

        mockMvc.perform(post("/admin/api/v1/products")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should update product and return status code 200")
    void updateTestCase1() throws Exception {
        ProductAdminResponse productResponse = new ProductAdminResponse(
                UUID.randomUUID(),
                "updated name",
                null,
                new ProductCategoryResponse(1, "testing"),
                Instant.now(),
                Instant.now()
        );

        String requestBody = """
            {
                "name": "updated name",
                "description": null,
                "categoryId": null
            }
        """;

        when(adminProductService.updateProduct(any(), any())).thenReturn(productResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", productResponse
        ));

        mockMvc.perform(patch("/admin/api/v1/products/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andExpect(content().json(expectedJSON));

        verify(adminProductService).updateProduct(any(), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void updateTestCase2() throws Exception {
        String requestBody = """
            {
                "name": "updated name"
            }
        """;

        mockMvc.perform(patch("/admin/api/v1/products/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void updateTestCase3() throws Exception {
        String requestBody = """
            {
                "name": "updated name"
            }
        """;

        mockMvc.perform(patch("/admin/api/v1/products/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product not exists")
    void updateTestCase4() throws Exception {
        String requestBody = """
            {
                "name": "updated name"
            }
        """;

        when(adminProductService.updateProduct(any(), any()))
                .thenThrow(ProductNotFoundException.class);

        mockMvc.perform(patch("/admin/api/v1/products/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should delete product and return status code 200")
    void deleteTestCase1() throws Exception {
        doNothing().when(adminProductService).deleteProductById(any());

        mockMvc.perform(delete("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isOk());

        verify(adminProductService).deleteProductById(any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteTestCase2() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void deleteTestCase3() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product not exists")
    void deleteTestCase4() throws Exception {
        doThrow(ProductNotFoundException.class)
                .when(adminProductService).deleteProductById(any());

        mockMvc.perform(delete("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 400 if product has active SKU")
    void deleteTestCase5() throws Exception {
        doThrow(CantDeleteProductException.class)
                .when(adminProductService).deleteProductById(any());

        mockMvc.perform(delete("/admin/api/v1/products/" + UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }
}
