package com.products.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class AdminProductResponse{
    private String id;
    private String name;
    private String description;
    private CategorySummary category;
    private List<SKU> SKUs;

    private List<ImageResponse> images;

    private Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;

    public static record CategorySummary(
            Long id,
            String name
    ){}

    public static record ImageResponse (
            UUID id,
            URL url
    ){}

    public static record SKU(
            String SKU,
            String name,
            Long stock,
            Price basePrice,
            Price currentPrice,
            List<Attribute> attributes,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy

    ){}

    public static record Price(
            String label,
            BigDecimal value
    ){}

    public static record Attribute(
            String name,
            String value
    ){}
}
