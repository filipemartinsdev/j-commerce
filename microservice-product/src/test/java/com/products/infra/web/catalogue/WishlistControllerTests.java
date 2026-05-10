package com.products.infra.web.catalogue;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.CreateWishlistItemRequest;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.WishlistItemAlreadyExistsException;
import com.products.application.exception.WishlistItemNotFoundException;
import com.products.application.service.WishlistService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@Import(SecurityConfig.class)
public class WishlistControllerTests {
    @MockitoBean private WishlistService wishlistService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should retrieve wishlist items and return status code 200")
    void getWishlistTestCase1() throws Exception {
        WishlistItemResponse itemResponse = new WishlistItemResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testing",
                new ProductPriceCatalogueResponse(
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(90),
                        10,
                        "Base"
                )
        );

        var expectedResponse = PagedResponse.<WishlistItemResponse>builder()
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .content(List.of(itemResponse))
                .build();

        when(wishlistService.getAllItems(any(), any()))
                .thenReturn(expectedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", expectedResponse
        ));

        mockMvc.perform(get("/api/v1/wishlist")
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void getWishlistTestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should add item to wishlist and return status code 201")
    void addItemToWishlistTestCase1() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 1
            }
        """.formatted(UUID.randomUUID());

        doNothing().when(wishlistService).createItem(any(), any());

        mockMvc.perform(post("/api/v1/wishlist")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isCreated());

        verify(wishlistService).createItem(any(CreateWishlistItemRequest.class), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void addItemToWishlistTestCase2() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 1
            }
        """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/wishlist")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 404 if product SKU not exists")
    void addItemToWishlistTestCase3() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 1
            }
        """.formatted(UUID.randomUUID());

        doThrow(ProductSKUNotFoundException.class)
                .when(wishlistService).createItem(any(), any());

        mockMvc.perform(post("/api/v1/wishlist")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 400 if item already exists in wishlist")
    void addItemToWishlistTestCase4() throws Exception {
        String requestBody = """
            {
                "productSKUId": "%s",
                "units": 1
            }
        """.formatted(UUID.randomUUID());

        doThrow(WishlistItemAlreadyExistsException.class)
                .when(wishlistService).createItem(any(), any());

        mockMvc.perform(post("/api/v1/wishlist")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
        ).andExpect(status().isBadRequest());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should delete item from wishlist and return status code 200")
    void deleteItemFromWishlistTestCase1() throws Exception {
        doNothing().when(wishlistService).deleteItem(any(), any());

        mockMvc.perform(delete("/api/v1/wishlist/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk());

        verify(wishlistService).deleteItem(any(), any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteItemFromWishlistTestCase2() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should return status code 404 if item not exists")
    void deleteItemFromWishlistTestCase3() throws Exception {
        doThrow(WishlistItemNotFoundException.class)
                .when(wishlistService).deleteItem(any(), any());

        mockMvc.perform(delete("/api/v1/wishlist/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(authorities = "SCOPE_USER")
    @Test @DisplayName("Should delete all items from wishlist and return status code 200")
    void deleteAllItemsTestCase1() throws Exception {
        doNothing().when(wishlistService).deleteAllItemsByUserId(any());

        mockMvc.perform(delete("/api/v1/wishlist")
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk());

        verify(wishlistService).deleteAllItemsByUserId(any());
    }

    @Test @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteAllItemsTestCase2() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist"))
                .andExpect(status().isUnauthorized());
    }
}
