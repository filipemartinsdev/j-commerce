package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "productCategories")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductCategory {
    @Id
    private Long id;

    @NotBlank @Length(max = 50)
    private String name;

    @NotNull
    private Instant createdAt = Instant.now();

    @NotNull
    private UUID createdBy;

    private Instant updatedAt;

    private UUID updatedBy;
}