package com.orders.application.service;

import com.orders.application.dto.AddressByCoordinatesResponse;
import com.orders.config.NominatimFeignClientConfig;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "nominatimClient", url = "https://nominatim.openstreetmap.org", configuration = NominatimFeignClientConfig.class)
public interface NominatimClient {

    @GetMapping("/reverse.php")
    ResponseEntity<AddressByCoordinatesResponse> getAddressByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam String format
    );
}
