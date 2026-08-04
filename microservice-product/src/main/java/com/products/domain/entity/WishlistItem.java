package com.products.domain.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "wishlistItems")
@Data @NoArgsConstructor @AllArgsConstructor
public class WishlistItem {
    @Id
    private String id;

    @Indexed @NotNull
    private UUID userId;

    @Indexed @NotNull
    private String productId;

    private String productName;
}
