package com.products.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

/*
* Projection to retrieve SemanticProductCatalogueView entities without embedding attribute.
* This approach improve performance because don't load a vector(1536) on memory for each entity.
* See SemanticProductCatalogueView.
* **/
public interface SemanticProductCatalogueProjection {
    UUID getProductId();
    String getName();
    String getDescription();
    Integer getCategoryId();
    String getCategoryName();
    BigDecimal getOriginalPriceValue();
    Integer getCurrentPriceTypeId();
    String getCurrentPriceTypeName();
    BigDecimal getCurrentPriceValue();
    Integer getStockCount();
    Float getDistance();
}
