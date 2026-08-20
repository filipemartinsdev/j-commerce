package com.products.domain.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Document(collection = "products")
@Data @AllArgsConstructor @NoArgsConstructor
public class Product {
    @Id
    private String id;

    @NotBlank @Length(max = 255)
    private String name;

    private String description;

    @NotNull
    private CategorySummary category;

    @NotNull
    private List<ProductSKU> SKUs = new ArrayList<>();

    @NotNull
    private Instant createdAt = Instant.now();

    @NotNull
    private UUID createdBy;

    private Instant updatedAt;

    private UUID updatedBy;

    public Optional<ProductSKU> findSKU(String SKU){
        return this.SKUs
                .stream()
                .filter(entity -> entity.getSKU().equals(SKU))
                .findFirst();
    }

    public boolean hasSKU(String SKU){
        return this.SKUs
                .stream()
                .anyMatch(entity -> entity.getSKU().equals(SKU));
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class CategorySummary {
        @Indexed
        private Long id;

        @NotEmpty @Length(max = 50)
        private String name;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class ProductSKU {
        @Indexed(unique = true)
        @NotEmpty
        private String SKU;

        @NotBlank @Length(max = 255)
        private String name;

        @NotNull @Min(0)
        private Long stock = 0L;

        private Price basePrice;

        private Price currentPrice;

        private List<Attribute> attributes;

        @NotNull
        private Instant createdAt = Instant.now();

        @NotNull
        private UUID createdBy;

        private Instant updatedAt;

        private UUID updatedBy;


        @Data @AllArgsConstructor @NoArgsConstructor
        public static class Price {
            @NotEmpty @Length(max = 255)
            private String label;

            @NotNull @Positive
            private BigDecimal value;
        }

        @Data @AllArgsConstructor @NoArgsConstructor
        public static class Attribute {
            @NotEmpty
            private String name;

            @NotEmpty
            private String value;
        }
    }
}
