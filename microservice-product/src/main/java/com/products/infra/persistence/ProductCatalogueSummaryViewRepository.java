package com.products.infra.persistence;

import com.products.domain.entity.ProductCatalogueSummaryView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductCatalogueSummaryViewRepository extends JpaRepository<ProductCatalogueSummaryView, UUID> {
    List<ProductCatalogueSummaryView> findAllByProductId(UUID productId);
}
