package com.products.infra.web.admin;

import com.products.application.dto.admin.CreateStockEntryRequest;
import com.products.application.dto.admin.ProductStockResponse;
import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.application.exception.ProductStockNotFoundException;
import com.products.application.service.ProductStockManagementService;
import com.products.application.service.StockMovementTypeService;
import com.products.application.service.mapper.StockMovementTypeMapper;
import com.products.config.SecurityConfig;
import com.products.infra.persistence.StockMovementTypeRepository;
import io.github.responsekit.core.PagedResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminStockController.class)
@Import(SecurityConfig.class)
public class AdminStockControllerTests {
    @MockitoBean private ProductStockManagementService adminProductStockService;
    @MockitoBean private StockMovementTypeService stockMovementTypeService;
    @MockitoBean private StockMovementTypeRepository stockMovementTypeRepository;
    @MockitoBean private StockMovementTypeMapper stockMovementTypeMapper;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve all stock and return status code 200")
    void getAllStockTestCase1() throws Exception {
        ProductStockResponse stockResponse = new ProductStockResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                "SKU-001",
                100,
                Instant.now()
        );

        var expectedResponse = PagedResponse
                .content(List.of(stockResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(adminProductStockService.getAll(any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/stock"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve stock by product ID and return status code 200")
    void getAllStockTestCase2() throws Exception {
        ProductStockResponse stockResponse = new ProductStockResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                "SKU-001",
                100,
                Instant.now()
        );

        var expectedResponse = PagedResponse
                .content(List.of(stockResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(adminProductStockService.getAllByProductId(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/stock")
                        .param("productId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllStockTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/stock"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not stock manager or admin")
    void getAllStockTestCase4() throws Exception {
        mockMvc.perform(get("/admin/api/v1/stock"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should create stock entry and return status code 201")
    void createStockEntryTestCase1() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 50
            }
        """.formatted(UUID.randomUUID());

        doNothing().when(adminProductStockService).createStockEntry(any(), any());

        mockMvc.perform(post("/admin/api/v1/stock/entries")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
        ).andExpect(status().isCreated());

        verify(adminProductStockService).createStockEntry(any(CreateStockEntryRequest.class), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void createStockEntryTestCase2() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 50
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/admin/api/v1/stock/entries")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not stock manager or admin")
    void createStockEntryTestCase3() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 50
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/admin/api/v1/stock/entries")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 404 if product stock not found")
    void createStockEntryTestCase4() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 50
            }
        """.formatted(UUID.randomUUID());

        doThrow(ProductStockNotFoundException.class)
                .when(adminProductStockService).createStockEntry(any(), any());

        mockMvc.perform(post("/admin/api/v1/stock/entries")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                        .authorities(List.of(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve all stock movements and return status code 200")
    void getAllStockMovementsTestCase1() throws Exception {
        StockMovementResponse movementResponse = new StockMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                50,
                new StockMovementTypeResponse(1, "Entry"),
                Instant.now(),
                UUID.randomUUID()
        );

        var expectedResponse = PagedResponse
                .content(List.of(movementResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(adminProductStockService.getAllMovements(any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/stock/movements"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve stock movements by product SKU ID and return status code 200")
    void getAllStockMovementsTestCase2() throws Exception {
        StockMovementResponse movementResponse = new StockMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                50,
                new StockMovementTypeResponse(1, "Entry"),
                Instant.now(),
                UUID.randomUUID()
        );

        var expectedResponse = PagedResponse
                .content(List.of(movementResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(adminProductStockService.getAllMovementsByProductSKUId(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/stock/movements")
                        .param("productSKUId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllStockMovementsTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/stock/movements"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not stock manager or admin")
    void getAllStockMovementsTestCase4() throws Exception {
        mockMvc.perform(get("/admin/api/v1/stock/movements"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve all stock movement types and return status code 200")
    void getAllStockMovementsTypesTestCase1() throws Exception {
        List<StockMovementTypeResponse> types = List.of(
                new StockMovementTypeResponse(1, "Entry"),
                new StockMovementTypeResponse(2, "Sale"),
                new StockMovementTypeResponse(3, "Refund")
        );

        when(stockMovementTypeService.getAll()).thenReturn(types);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", types
        ));

        mockMvc.perform(get("/admin/api/v1/stock/movements/types"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllStockMovementsTypesTestCase2() throws Exception {
        mockMvc.perform(get("/admin/api/v1/stock/movements/types"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client is not stock manager or admin")
    void getAllStockMovementsTypesTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/stock/movements/types"))
                .andExpect(status().isForbidden());
    }
}
