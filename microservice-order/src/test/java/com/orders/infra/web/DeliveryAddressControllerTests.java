package com.orders.infra.web;

import com.orders.application.dto.CreateDeliveryAddressRequest;
import com.orders.application.dto.DeliveryAddressResponse;
import com.orders.application.dto.UpdateDeliveryAddressRequest;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.service.DeliveryAddressService;
import com.orders.config.SecurityConfig;
import io.github.responsekit.core.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryAddressController.class)
@Import(SecurityConfig.class)
public class DeliveryAddressControllerTests {

    @MockitoBean
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return paginated addresses and status code 200")
    void getAllAddressesByUserTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var response = new DeliveryAddressResponse(
                addressId,
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

        var pagedResponse = PagedResponse
                .content(List.of(response))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(deliveryAddressService.getAllByUserId(eq(userId), any()))
                .thenReturn(pagedResponse);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", pagedResponse
        ));

        mockMvc.perform(get("/api/v1/delivery-addresses")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void getAllAddressesByUserTestCase2() throws Exception {
        mockMvc.perform(get("/api/v1/delivery-addresses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong role")
    void getAllAddressesByUserTestCase3() throws Exception {
        mockMvc.perform(get("/api/v1/delivery-addresses")
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return address by salesOrderId and status code 200")
    void getAddressByIdTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var response = new DeliveryAddressResponse(
                addressId,
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

        when(deliveryAddressService.getById(eq(addressId), eq(userId)))
                .thenReturn(response);

        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", response
        ));

        mockMvc.perform(get("/api/v1/delivery-addresses/{id}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void getAddressByIdTestCase2() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/delivery-addresses/{id}", addressId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong role")
    void getAddressByIdTestCase3() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/delivery-addresses/{id}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status code 404 if address not found")
    void getAddressByIdTestCase4() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(deliveryAddressService.getById(eq(addressId), eq(userId)))
                .thenThrow(DeliveryAddressNotFoundException.class);

        mockMvc.perform(get("/api/v1/delivery-addresses/{id}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create address and return status code 201")
    void createAddressTestCase1() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        var request = new CreateDeliveryAddressRequest(
                false,
                Optional.of("12345678"),
                Optional.of("Test Street"),
                Optional.of("123"),
                Optional.of("Apt 1"),
                Optional.of("Test Neighborhood"),
                Optional.of("Test City"),
                Optional.of("TS"),
                Optional.empty(),
                Optional.empty()
        );

        var response = new DeliveryAddressResponse(
                addressId,
                "12345678",
                "Test Street",
                "123",
                "Apt 1",
                "Test Neighborhood",
                "Test City",
                "TS",
                null,
                null,
                Instant.now()
        );

        when(deliveryAddressService.createByUserId(any(), eq(userId)))
                .thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);
        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", response
        ));

        mockMvc.perform(post("/api/v1/delivery-addresses")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should create address by coordinates and return status code 201")
    void createAddressTestCase2() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        var request = new CreateDeliveryAddressRequest(
                true,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("Apt 1"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(-23.0),
                Optional.of(-46.0)
        );

        var response = new DeliveryAddressResponse(
                addressId,
                "12345678",
                "Test Street",
                "S/N",
                "Apt 1",
                "Test Neighborhood",
                "Test City",
                "TS",
                -23.0,
                -46.0,
                Instant.now()
        );

        when(deliveryAddressService.createByCoordinatesAndUserId(any(), eq(userId)))
                .thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);
        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", response
        ));

        mockMvc.perform(post("/api/v1/delivery-addresses")
                        .param("byCoordinates", "true")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void createAddressTestCase3() throws Exception {
        var request = new CreateDeliveryAddressRequest(
                false,
                Optional.of("12345678"),
                Optional.of("Test Street"),
                Optional.of("123"),
                Optional.empty(),
                Optional.of("Test Neighborhood"),
                Optional.of("Test City"),
                Optional.of("TS"),
                Optional.empty(),
                Optional.empty()
        );

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/delivery-addresses")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong role")
    void createAddressTestCase4() throws Exception {
        var request = new CreateDeliveryAddressRequest(
                false,
                Optional.of("12345678"),
                Optional.of("Test Street"),
                Optional.of("123"),
                Optional.empty(),
                Optional.of("Test Neighborhood"),
                Optional.of("Test City"),
                Optional.of("TS"),
                Optional.empty(),
                Optional.empty()
        );

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/delivery-addresses")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete address and return status code 201")
    void deleteAddressTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(deliveryAddressService).deleteById(addressId, userId);

        mockMvc.perform(delete("/api/v1/delivery-addresses/{id}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void deleteAddressTestCase2() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/delivery-addresses/{id}", addressId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong role")
    void deleteAddressTestCase3() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/delivery-addresses/{id}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_STOCK_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status code 404 if address not found")
    void deleteAddressTestCase4() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(DeliveryAddressNotFoundException.class)
                .when(deliveryAddressService).deleteById(addressId, userId);

        mockMvc.perform(delete("/api/v1/delivery-addresses/{id}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update address and return status code 201")
    void updateAddressTestCase1() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.of("Updated Street"),
                Optional.of("456"),
                Optional.of("Apt 2"),
                Optional.of("New Neighborhood"),
                Optional.of("New City"),
                Optional.of("NS"),
                Optional.empty(),
                Optional.empty()
        );

        var response = new DeliveryAddressResponse(
                addressId,
                "87654321",
                "Updated Street",
                "456",
                "Apt 2",
                "New Neighborhood",
                "New City",
                "NS",
                null,
                null,
                Instant.now()
        );

        when(deliveryAddressService.updateById(eq(addressId), eq(userId), any()))
                .thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(request);
        String expectedJSON = objectMapper.writeValueAsString(Map.of(
                "status", "success",
                "data", response
        ));

        mockMvc.perform(patch("/api/v1/delivery-addresses/{id}", addressId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJSON));
    }

    @Test
    @DisplayName("Should return status code 401 if client is not authenticated")
    void updateAddressTestCase2() throws Exception {
        UUID addressId = UUID.randomUUID();

        var request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/delivery-addresses/{id}", addressId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return status code 403 if wrong role")
    void updateAddressTestCase3() throws Exception {
        UUID addressId = UUID.randomUUID();

        var request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.of("Updated Street"),
                Optional.of("456"),
                Optional.of("Apt 2"),
                Optional.of("New Neighborhood"),
                Optional.of("New City"),
                Optional.of("NS"),
                Optional.empty(),
                Optional.empty()
        );

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/delivery-addresses/{id}", addressId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return status code 404 if address not found")
    void updateAddressTestCase4() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var request = new UpdateDeliveryAddressRequest(
                Optional.of("87654321"),
                Optional.of("Updated Street"),
                Optional.of("456"),
                Optional.of("Apt 2"),
                Optional.of("New Neighborhood"),
                Optional.of("New City"),
                Optional.of("NS"),
                Optional.empty(),
                Optional.empty()
        );

        when(deliveryAddressService.updateById(eq(addressId), eq(userId), any()))
                .thenThrow(DeliveryAddressNotFoundException.class);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/delivery-addresses/{id}", addressId)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }
}