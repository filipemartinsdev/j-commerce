package com.products.infra.persistence;

import com.products.domain.entity.ProductSKUSummaryCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductSKUSummaryCatalogueRepository extends JpaRepository<ProductSKUSummaryCatalogue, UUID> {
    List<ProductSKUSummaryCatalogue> findAllByProductId(UUID productId);
}
