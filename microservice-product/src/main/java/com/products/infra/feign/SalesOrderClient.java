package com.products.infra.feign;

import com.products.config.SalesOrderFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "order", url = "${feign.clients.sales_order.url}", configuration = SalesOrderFeignClientConfig.class)
public interface SalesOrderClient {
    @GetMapping("/api/v1/delivery-addresses/{id}")
    ResponseEntity<Map<String, Object>> getDeliveryAddress(@PathVariable UUID id, @RequestHeader("Authorization") String JWT);
}
