package com.products.domain.entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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

    @NotEmpty
    private String name;
}
