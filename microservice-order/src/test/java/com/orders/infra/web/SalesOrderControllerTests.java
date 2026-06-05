package com.orders.infra.web;

import com.orders.config.SecurityConfig;
import com.orders.application.dto.*;
import com.orders.application.exception.*;
import com.orders.application.service.SalesOrderService;
import io.github.responsekit.core.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(SalesOrderController.class)
@Import(SecurityConfig.class)
public class SalesOrderControllerTests {

    @MockitoBean
    private SalesOrderService salesOrderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return paginated sales orders and status code 200")
    void getAllSalesOrdersTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        var response = new SalesOrderResponse(
                orderId,
                Instant.now(),
                "PENDING",
                new BigDecimal("100.00")
        );

        var pagedResponse = PagedResponse
                .content(List.of(response))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(salesOrderService.getAllByUserId(eq(userId), any()))
                .thenReturn(pagedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", pagedResponse
        ));

        mockMvc.perform(get("/api/v1/sales-orders")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllSalesOrdersTestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/sales-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong scope")
    void getAllSalesOrdersTestCase3() throws Exception {
        mockMvc.perform(get("/api/v1/sales-orders")
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return sales order summary and status code 200")
    void getSalesOrderByIdTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        var itemResponse = new SalesOrderItemResponse(
                UUID.randomUUID(),
                "Product Name",
                new BigDecimal("50.00"),
                2
        );

        var deliveryAddressResponse = new DeliveryAddressResponse(
                UUID.randomUUID(),
                "12345678",
                "Test Street",
                "123",
                "Apt 1",
                "Test Neighborhood",
                "Test City",
                "TS",
                -23.0,
                -46.0,
                Instant.now()
        );

        var response = new SalesOrderSummaryResponse(
                orderId,
                "PENDING",
                new BigDecimal("100.00"),
                List.of(itemResponse),
                deliveryAddressResponse,
                null,
                Instant.now()
        );

        when(salesOrderService.getSummaryById(eq(orderId), eq(userId)))
                .thenReturn(response);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", response
        ));

        mockMvc.perform(get("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void getSalesOrderByIdTestCase2() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/sales-orders/{id}", orderId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong scope")
    void getSalesOrderByIdTestCase3() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status code 404 if sales order not found")
    void getSalesOrderByIdTestCase4() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(salesOrderService.getSummaryById(eq(orderId), eq(userId)))
                .thenThrow(SalesOrderNotFoundException.class);

        mockMvc.perform(get("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should cancel sales order and return status code 200")
    void requestToCancelSalesOrderTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        doNothing().when(salesOrderService).requestToCancelOrder(orderId, userId);

        mockMvc.perform(delete("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void requestToCancelSalesOrderTestCase2() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/sales-orders/{id}", orderId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong scope")
    void requestToCancelSalesOrderTestCase3() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status code 404 if sales order not found")
    void requestToCancelSalesOrderTestCase4() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        doThrow(SalesOrderNotFoundException.class)
                .when(salesOrderService).requestToCancelOrder(orderId, userId);

        mockMvc.perform(delete("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return status code 400 if cant cancel sales order")
    void requestToCancelSalesOrderTestCase5() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        doThrow(CantCancelSalesOrderException.class)
                .when(salesOrderService).requestToCancelOrder(orderId, userId);

        mockMvc.perform(delete("/api/v1/sales-orders/{id}", orderId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isBadRequest());
    }
}