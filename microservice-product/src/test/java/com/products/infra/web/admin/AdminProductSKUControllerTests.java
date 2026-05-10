package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateProductSKURequest;
import com.products.application.dto.admin.ProductSKUAdminResponse;
import com.products.application.exception.ProductNotActiveException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.SKUAlreadyInUseException;
import com.products.application.service.ProductSKUManagementService;
import com.products.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductSKUController.class)
@Import(SecurityConfig.class)
public class AdminProductSKUControllerTests {
    @MockitoBean private ProductSKUManagementService adminProductSKUService;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve all product SKUs and return status code 200")
    void getAllProductSKUsTestCase1() throws Exception {
        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );

        var expectedResponse = PagedResponse.<ProductSKUAdminResponse>builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .content(List.of(skuResponse))
                .build();


        when(adminProductSKUService.getAllProductSKUs(any()))
                .thenReturn(expectedResponse);


        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/skus"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve product SKUs by product ID and return status code 200 if user is admin")
    void getAllProductSKUsTestCase2() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                UUID.randomUUID(),
                productId,
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );

        var expectedResponse = PagedResponse.<ProductSKUAdminResponse>builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .content(List.of(skuResponse))
                .build();

        when(adminProductSKUService.getAllProductSKUsByProductId(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/skus")
                .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = {"SCOPE_USER", "SCOPE_STOCK_MANAGER"})
    @Test @DisplayName("Should retrieve product SKUs by product ID and return status code 200 if user is stock manager")
    void getAllProductSKUsTestCase3() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                UUID.randomUUID(),
                productId,
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );

        var expectedResponse = PagedResponse.<ProductSKUAdminResponse>builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .content(List.of(skuResponse))
                .build();


        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        when(adminProductSKUService.getAllProductSKUsByProductId(any(), any()))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/admin/api/v1/skus")
                .param("productId", productId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllProductSKUsTestCase4() throws Exception {
        mockMvc.perform(get("/admin/api/v1/skus"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = {"SCOPE_USER", "SCOPE_DRIVER"})
    @Test @DisplayName("Should return status code 403 if client is not admin or stock manager")
    void getAllProductSKUsTestCase5() throws Exception {
        mockMvc.perform(get("/admin/api/v1/skus"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve product SKU by ID and return status code 200 if user is admin")
    void getProductSKUByIdTestCase1() throws Exception {
        UUID skuId = UUID.randomUUID();
        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                skuId,
                UUID.randomUUID(),
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );


        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", skuResponse
        ));

        when(adminProductSKUService.getProductSKUById(any())).thenReturn(skuResponse);

        mockMvc.perform(get("/admin/api/v1/skus/" + skuId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_STOCK_MANAGER")
    @Test @DisplayName("Should retrieve product SKU by ID and return status code 200 if user is admin")
    void getProductSKUByIdTestCase2() throws Exception {
        UUID skuId = UUID.randomUUID();
        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                skuId,
                UUID.randomUUID(),
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", skuResponse
        ));

        when(adminProductSKUService.getProductSKUById(any())).thenReturn(skuResponse);

        mockMvc.perform(get("/admin/api/v1/skus/" + skuId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product SKU not exists")
    void getProductSKUByIdTestCase3() throws Exception {
        when(adminProductSKUService.getProductSKUById(any()))
                .thenThrow(ProductSKUNotFoundException.class);

        mockMvc.perform(get("/admin/api/v1/skus/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getProductSKUByIdTestCase4() throws Exception {
        mockMvc.perform(get("/admin/api/v1/skus/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should return status code 403 if client is not admin or stock manager")
    @WithMockUser(authorities = "SCOPE_USER")
    void getProductSKUByIdTestCase5() throws Exception {
        mockMvc.perform(get("/admin/api/v1/skus/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should create product SKU and return status code 201 if user is admin")
    void createTestCase1() throws Exception {
        UUID productId = UUID.randomUUID();
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-001"
            }
        """.formatted(productId);

        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                UUID.randomUUID(),
                productId,
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", skuResponse
        ));

        when(adminProductSKUService.createProductSKU(any(), any())).thenReturn(skuResponse);

        mockMvc.perform(post("/admin/api/v1/skus")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                        jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())).authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN")))
                )
        ).andExpect(status().isCreated())
        .andExpect(content().json(expectedJSON));

        verify(adminProductSKUService)
                .createProductSKU(any(CreateProductSKURequest.class), any());
    }

    @WithMockUser(authorities = "SCOPE_STOCK_MANAGER")
    @Test @DisplayName("Should create product SKU and return status code 201 if user is stock manager")
    void createTestCase2() throws Exception {
        UUID productId = UUID.randomUUID();
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-001"
            }
        """.formatted(productId);

        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                UUID.randomUUID(),
                productId,
                "SKU-001",
                "testing",
                Instant.now(),
                null,
                true
        );

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", skuResponse
        ));

        when(adminProductSKUService.createProductSKU(any(), any())).thenReturn(skuResponse);

        mockMvc.perform(post("/admin/api/v1/skus")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
                ).andExpect(status().isCreated())
                .andExpect(content().json(expectedJSON));

        verify(adminProductSKUService).createProductSKU(any(CreateProductSKURequest.class), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void createTestCase3() throws Exception {
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-001"
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/admin/api/v1/skus")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = {"SCOPE_USER", "SCOPE_DRIVER"})
    @Test @DisplayName("Should return status code 403 if client is not admin or stock manager")
    void createTestCase4() throws Exception {
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-001"
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/admin/api/v1/skus")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test @DisplayName("Should return status code 404 if product not exists")
    void createTestCase5() throws Exception {
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-001"
            }
        """.formatted(UUID.randomUUID());

        when(adminProductSKUService.createProductSKU(any(), any()))
                .thenThrow(ProductNotFoundException.class);

        mockMvc.perform(post("/admin/api/v1/skus")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
        ).andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return status code 400 if SKU already in use")
    void createTestCase6() throws Exception {
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-DUPLICATE"
            }
        """.formatted(UUID.randomUUID());

        when(adminProductSKUService.createProductSKU(any(), any()))
                .thenThrow(SKUAlreadyInUseException.class);

        mockMvc.perform(post("/admin/api/v1/skus")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
        ).andExpect(status().isBadRequest());
    }

    @Test @DisplayName("Should return status code 400 if product is not active")
    void createTestCase7() throws Exception {
        String requestBody = """
            {
                "productId": "%s",
                "name": "testing",
                "SKU": "SKU-001"
            }
        """.formatted(UUID.randomUUID());

        when(adminProductSKUService.createProductSKU(any(), any()))
                .thenThrow(ProductNotActiveException.class);

        mockMvc.perform(post("/admin/api/v1/skus")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt()
                        .jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
        ).andExpect(status().isBadRequest());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should update product SKU and return status code 200")
    void updateTestCase1() throws Exception {
        UUID skuId = UUID.randomUUID();
        String requestBody = """
            {
                "name": "updated name",
                "SKU": "updated SKU"
            }
        """;

        ProductSKUAdminResponse skuResponse = new ProductSKUAdminResponse(
                skuId,
                UUID.randomUUID(),
                "updated SKU",
                "updated name",
                Instant.now(),
                Instant.now(),
                true
        );

        when(adminProductSKUService.updateProductSKU(any(), any())).thenReturn(skuResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", skuResponse
        ));

        mockMvc.perform(patch("/admin/api/v1/skus/" + skuId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));

        verify(adminProductSKUService).updateProductSKU(any(), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void updateTestCase2() throws Exception {
        String requestBody = """
            {
                "name": "updated name"
            }
        """;

        mockMvc.perform(patch("/admin/api/v1/skus/" + UUID.randomUUID())
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

        mockMvc.perform(patch("/admin/api/v1/skus/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product SKU not exists")
    void updateTestCase4() throws Exception {
        UUID skuId = UUID.randomUUID();
        String requestBody = """
            {
                "name": "updated name"
            }
        """;

        when(adminProductSKUService.updateProductSKU(any(), any()))
                .thenThrow(ProductSKUNotFoundException.class);

        mockMvc.perform(patch("/admin/api/v1/skus/" + skuId)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should delete product SKU and return status code 200")
    void deleteTestCase1() throws Exception {
        UUID skuId = UUID.randomUUID();

        doNothing().when(adminProductSKUService).deleteProductSKUById(any());

        mockMvc.perform(delete("/admin/api/v1/skus/" + skuId))
                .andExpect(status().isOk());

        verify(adminProductSKUService).deleteProductSKUById(any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteTestCase2() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/skus/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void deleteTestCase3() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/skus/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product SKU not exists")
    void deleteTestCase4() throws Exception {
        doThrow(ProductSKUNotFoundException.class)
                .when(adminProductSKUService).deleteProductSKUById(any());

        mockMvc.perform(delete("/admin/api/v1/skus/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
