package com.products.infra.persistence;

import com.products.domain.entity.ProductCatalogueView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductCatalogueViewRepository extends JpaRepository<ProductCatalogueView, UUID> {
    Page<ProductCatalogueView> findAllByCategoryId(Integer categoryId, Pageable pageable);
}
