package com.products.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

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
}
