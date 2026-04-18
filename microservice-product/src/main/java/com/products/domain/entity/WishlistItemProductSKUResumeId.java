package com.products.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemProductSKUResumeId {
    private UUID userId;
    private UUID productSKUId;
}
