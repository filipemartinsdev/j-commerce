package com.products.infra.web.admin;

import com.products.application.dto.PriceTypeResponse;
import com.products.application.dto.admin.CreateProductSKUPrice;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductSKUPriceNotFoundException;
import com.products.application.exception.ProductSKUWithoutBasePriceException;
import com.products.application.service.ProductPriceManagementService;
import com.products.config.SecurityConfig;
import io.github.responsekit.core.PagedResponse;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPriceController.class)
@Import(SecurityConfig.class)
public class AdminPriceControllerTests {
    @MockitoBean private ProductPriceManagementService productPriceService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve all prices and return status code 200")
    void getAllPricesTestCase1() throws Exception {
        ProductSKUPriceResponse priceResponse = new ProductSKUPriceResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                BigDecimal.ONE,
                new PriceTypeResponse(1, "Base"),
                Instant.now(),
                null,
                Instant.now()
        );

        var expectedResponse = PagedResponse
                .content(List.of(priceResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(productPriceService.getAllPrices(any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/prices"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve prices by product SKU ID and return status code 200")
    void getAllPricesTestCase2() throws Exception {
        ProductSKUPriceResponse priceResponse = new ProductSKUPriceResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                BigDecimal.ONE,
                new PriceTypeResponse(1, "Base"),
                Instant.now(),
                null,
                Instant.now()
        );

        var expectedResponse = PagedResponse
                .content(List.of(priceResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(productPriceService.getAllPricesByProductSKUId(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/prices")
                        .param("productSKUId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllPricesTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/prices"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void getAllPricesTestCase4() throws Exception {
        mockMvc.perform(get("/admin/api/v1/prices"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should create price and return status code 200")
    void createTestCase1() throws Exception {
        ProductSKUPriceResponse priceResponse = new ProductSKUPriceResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                BigDecimal.valueOf(100),
                new PriceTypeResponse(1, "Base"),
                Instant.now(),
                null,
                Instant.now()
        );

        String requestBody = """
            {
                "productSKUId": "%s",
                "price": 100.00,
                "priceTypeId": 1,
                "startAt": null,
                "endAt": null
            }
        """.formatted(priceResponse.productSKUId());

        when(productPriceService.create(any())).thenReturn(priceResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", priceResponse
        ));

        mockMvc.perform(post("/admin/api/v1/prices")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andExpect(content().json(expectedJSON));

        verify(productPriceService).create(any(CreateProductSKUPrice.class));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void createTestCase2() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "price": 100.00,
                "priceTypeId": 1
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/admin/api/v1/prices")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void createTestCase3() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "price": 100.00,
                "priceTypeId": 1
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/admin/api/v1/prices")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product SKU not exists")
    void createTestCase4() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "price": 100.00,
                "priceTypeId": 1
            }
        """.formatted(UUID.randomUUID());

        when(productPriceService.create(any()))
                .thenThrow(ProductSKUNotFoundException.class);

        mockMvc.perform(post("/admin/api/v1/prices")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 400 if special price without base price")
    void createTestCase5() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "price": 80.00,
                "priceTypeId": 2
            }
        """.formatted(UUID.randomUUID());

        when(productPriceService.create(any()))
                .thenThrow(ProductSKUWithoutBasePriceException.class);

        mockMvc.perform(post("/admin/api/v1/prices")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should update price and return status code 200")
    void updateTestCase1() throws Exception {
        ProductSKUPriceResponse priceResponse = new ProductSKUPriceResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                BigDecimal.valueOf(90),
                new PriceTypeResponse(1, "Base"),
                Instant.now(),
                null,
                Instant.now()
        );

        String requestBody = """
            {
                "price": 90.00,
                "priceType": null,
                "startAt": null,
                "endAt": null
            }
        """;

        when(productPriceService.update(any(), any())).thenReturn(priceResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", priceResponse
        ));

        mockMvc.perform(patch("/admin/api/v1/prices/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andExpect(content().json(expectedJSON));

        verify(productPriceService).update(any(), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void updateTestCase2() throws Exception {
        String requestBody = """
            {
                "price": 90.00
            }
        """;

        mockMvc.perform(patch("/admin/api/v1/prices/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void updateTestCase3() throws Exception {
        String requestBody = """
            {
                "price": 90.00
            }
        """;

        mockMvc.perform(patch("/admin/api/v1/prices/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if price not exists")
    void updateTestCase4() throws Exception {
        String requestBody = """
            {
                "price": 90.00
            }
        """;

        when(productPriceService.update(any(), any()))
                .thenThrow(ProductSKUPriceNotFoundException.class);

        mockMvc.perform(patch("/admin/api/v1/prices/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should delete price and return status code 200")
    void deleteTestCase1() throws Exception {
        doNothing().when(productPriceService).deleteById(any());

        mockMvc.perform(delete("/admin/api/v1/prices/" + UUID.randomUUID()))
                .andExpect(status().isOk());

        verify(productPriceService).deleteById(any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteTestCase2() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/prices/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not admin")
    void deleteTestCase3() throws Exception {
        mockMvc.perform(delete("/admin/api/v1/prices/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if price not exists")
    void deleteTestCase4() throws Exception {
        doThrow(ProductSKUPriceNotFoundException.class)
                .when(productPriceService).deleteById(any());

        mockMvc.perform(delete("/admin/api/v1/prices/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
