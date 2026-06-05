package com.products.infra.web.catalogue;

import com.products.application.dto.catalogue.ConfirmShoppingCartRequest;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.exception.*;
import com.products.application.service.ShoppingCartService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingCartController.class)
@Import(SecurityConfig.class)
public class ShoppingCartControllerTests {
    @MockitoBean private ShoppingCartService shoppingCartService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should retrieve shopping cart items and return status code 200")
    void getAllItemsTestCase1() throws Exception {
        ShoppingCartItemResponse itemResponse = new ShoppingCartItemResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                1,
                BigDecimal.ONE,
                BigDecimal.ONE,
                0
        );

        var expectedResponse = PagedResponse
                .content(List.of(itemResponse))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(shoppingCartService.getAllItems(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/api/v1/shopping-cart")
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllItemsTestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/shopping-cart"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should create new shopping cart item and return status code 201")
    void createTestCase1() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 2
            }
        """.formatted(UUID.randomUUID());

        doNothing().when(shoppingCartService).createItemByUserId(any(), any());

        mockMvc.perform(post("/api/v1/shopping-cart")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isCreated());

        verify(shoppingCartService).createItemByUserId(any(CreateShoppingCartItemRequest.class), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void createTestCase2() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 2
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/shopping-cart")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 404 if product not exists")
    void createTestCase3() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 2
            }
        """.formatted(UUID.randomUUID());

        doThrow(ProductSKUNotFoundException.class)
                .when(shoppingCartService).createItemByUserId(any(), any());

        mockMvc.perform(post("/api/v1/shopping-cart")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 422 if product haven't stock enough")
    void createTestCase4() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 9999
            }
        """.formatted(UUID.randomUUID());

        doThrow(ProductOutOfStockException.class)
                .when(shoppingCartService).createItemByUserId(any(), any());

        mockMvc.perform(post("/api/v1/shopping-cart")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isUnprocessableContent());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 400 if product is already on shopping cart")
    void createTestCase5() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 2
            }
        """.formatted(UUID.randomUUID());

        doThrow(ShoppingCartItemAlreadyExistsException.class)
                .when(shoppingCartService).createItemByUserId(any(), any());

        mockMvc.perform(post("/api/v1/shopping-cart")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isBadRequest());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should clean shopping cart and return status code 200")
    void deleteAllItemsTestCase1() throws Exception {
        doNothing().when(shoppingCartService).deleteAllItemsByUserId(any());

        mockMvc.perform(delete("/api/v1/shopping-cart")
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk());

        verify(shoppingCartService).deleteAllItemsByUserId(any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteAllItemsTestCase2() throws Exception {
        mockMvc.perform(delete("/api/v1/shopping-cart"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should remove item from shopping cart and return status code 200")
    void deleteByIdTestCase1() throws Exception {
        doNothing().when(shoppingCartService).deleteItemById(any(), any());

        mockMvc.perform(delete("/api/v1/shopping-cart/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk());

        verify(shoppingCartService).deleteItemById(any(), any());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 404 if item not exists")
    void deleteByIdTestCase2() throws Exception {
        doThrow(ShoppingCartItemNotFoundException.class)
                .when(shoppingCartService).deleteItemById(any(), any());

        mockMvc.perform(delete("/api/v1/shopping-cart/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteByIdTestCase3() throws Exception {
        mockMvc.perform(delete("/api/v1/shopping-cart/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should confirm shopping cart and return status code 201")
    void confirmTestCase1() throws Exception {
        String requestBody = """
            {
                "deliveryAddressId": "%s"
            }
        """.formatted(UUID.randomUUID());

        doNothing().when(shoppingCartService).confirmShoppingCart(any(), any(), any());

        mockMvc.perform(post("/api/v1/shopping-cart/checkout")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isCreated());

        verify(shoppingCartService).confirmShoppingCart(any(ConfirmShoppingCartRequest.class), any(), any());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 404 if delivery address not exists")
    void confirmTestCase2() throws Exception {
        String requestBody = """
            {
                "deliveryAddressId": "%s"
            }
        """.formatted(UUID.randomUUID());

        doThrow(DeliveryAddressNotFoundException.class)
                .when(shoppingCartService).confirmShoppingCart(any(), any(), any());

        mockMvc.perform(post("/api/v1/shopping-cart/checkout")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isNotFound());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void confirmTestCase3() throws Exception {
        String requestBody = """
            {
                "deliveryAddressId": "%s"
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/shopping-cart/checkout")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }
}