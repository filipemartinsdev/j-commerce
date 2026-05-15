package com.orders.infra.web;

import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.ShippingResponse;
import com.orders.application.service.AdminShippingService;
import com.orders.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AdminShippingController.class)
@Import(SecurityConfig.class)
public class AdminShippingControllerTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AdminShippingService adminShippingService;

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should retrieve shipments and return status code 200")
    void getAllTestCase1() throws Exception {
        UUID shippingId = UUID.randomUUID();
        UUID salesOrderId = UUID.randomUUID();
        UUID deliveryAddressId = UUID.randomUUID();

        var response = new ShippingResponse(
                shippingId,
                "PENDING",
                salesOrderId,
                deliveryAddressId,
                Instant.now(),
                null,
                Instant.now()
        );

        var pagedResponse = PagedResponse.<ShippingResponse>builder()
                .page(0).size(20).totalElements(1L).totalPages(1)
                .isLast(true).content(List.of(response)).build();

        when(adminShippingService.getAll(any())).thenReturn(pagedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success", "data", pagedResponse
        ));

        mockMvc.perform(get("/admin/api/v1/shippings"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllTestCase2() throws Exception {
        mockMvc.perform(get("/admin/api/v1/shippings"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 403 if client scope is not ADMIN, LOGISTICS or DRIVER")
    void getAllTestCase3() throws Exception {
        mockMvc.perform(get("/admin/api/v1/shippings"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should dispatch and return status code 200")
    void dispatchShippingTestCase1() throws Exception {
        UUID shippingId = UUID.randomUUID();

        doNothing().when(adminShippingService).dispatchShipping(shippingId);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success"
        ));

        mockMvc.perform(post("/admin/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void dispatchShippingTestCase2() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(post("/admin/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_DRIVER")
    @Test @DisplayName("Should return status code 403 if client scope is not ADMIN or LOGISTICS")
    void dispatchShippingTestCase3() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(post("/admin/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("Should check-in and return status code 200")
    void checkInShippingTestCase1() throws Exception {
        UUID driverId = UUID.randomUUID();
        UUID shippingId = UUID.randomUUID();

        doNothing().when(adminShippingService).startShipping(shippingId, driverId);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success"
        ));

        mockMvc.perform(post("/admin/api/v1/shippings/{id}/check-in", shippingId)
                        .with(jwt().jwt(jwt -> jwt.subject(driverId.toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_DRIVER")))
        ).andExpect(status().isOk())
          .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void checkInShippingTestCase2() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(post("/admin/api/v1/shippings/{id}/check-in", shippingId))
                .andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("Should return status code 403 if client scope is not DRIVER")
    void checkInShippingTestCase3() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(post("/admin/api/v1/shippings/{id}/check-in", shippingId)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("SCOPE_ADMIN")))
        ).andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_DRIVER")
    @Test @DisplayName("Should check-out and return status code 200")
    void checkOutShippingTestCase1() throws Exception {
        UUID shippingId = UUID.randomUUID();

        doNothing().when(adminShippingService).finishShipping(shippingId);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success"
        ));

        mockMvc.perform(post("/admin/api/v1/shippings/{id}/check-out", shippingId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void checkOutShippingTestCase2() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(post("/admin/api/v1/shippings/{id}/check-out", shippingId))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_ADMIN")
    @Test @DisplayName("Should return status code 403 if client scope is not DRIVER")
    void checkOutShippingTestCase3() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(post("/admin/api/v1/shippings/{id}/check-out", shippingId))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities = "SCOPE_LOGISTICS")
    @Test @DisplayName("Should cancel shipping and return status code 200")
    void cancelShippingTestCase1() throws Exception {
        UUID shippingId = UUID.randomUUID();

        doNothing().when(adminShippingService).cancelShipping(shippingId);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success"
        ));

        mockMvc.perform(delete("/admin/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void cancelShippingTestCase2() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_STOCK_MANAGER")
    @Test @DisplayName("Should return status code 403 if client scope is not DRIVER, LOGISTICS or ADMIN")
    void cancelShippingTestCase3() throws Exception {
        UUID shippingId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/api/v1/shippings/{id}", shippingId))
                .andExpect(status().isForbidden());
    }
}