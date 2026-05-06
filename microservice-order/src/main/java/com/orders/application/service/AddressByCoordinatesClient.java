package com.orders.application.service;

import com.orders.application.dto.AddressByCoordinatesResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "addressCoordinatesClient", url = "https://nominatim.openstreetmap.org")
public interface AddressByCoordinatesClient {
    @GetMapping("/reverse.php")
    ResponseEntity<AddressByCoordinatesResponse> getAddressByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam String format
    );
}
