package com.products.domain.projection;

import java.util.UUID;

public interface ProductEmbeddingProjection {
    UUID getId();

    String getProductId();

    Float getDistance();
}
