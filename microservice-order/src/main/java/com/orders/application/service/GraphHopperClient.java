package com.orders.application.service;

import com.orders.application.dto.GeocodingResponse;
import com.orders.application.dto.RouteRequest;
import com.orders.application.dto.RouteResponse;
import com.orders.config.GraphHopperFeignClientConfig;
import feign.Headers;
import io.swagger.v3.oas.annotations.headers.Header;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "graphHopperClient", url = "https://graphhopper.com", configuration = GraphHopperFeignClientConfig.class)
public interface GraphHopperClient {
    @PostMapping("/api/1/route")
    ResponseEntity<RouteResponse> route(
            @RequestParam String key,
            @Valid @RequestBody RouteRequest body
    );

    @GetMapping("/api/1/geocode")
    ResponseEntity<GeocodingResponse> geocode(
            @RequestParam String key,
            @RequestParam String q
    );
}
