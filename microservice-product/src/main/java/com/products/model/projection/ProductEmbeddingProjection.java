package com.products.model.projection;

import java.util.UUID;

public interface ProductEmbeddingProjection {
    UUID getId();

    String getProductId();

    Float getDistance();
}
