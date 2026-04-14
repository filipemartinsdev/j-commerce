package com.products.infra.persistence;

import com.products.domain.entity.ProductResumeCatalogue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductResumeCatalogueRepository extends JpaRepository<ProductResumeCatalogue, UUID> {
    Page<ProductResumeCatalogue> findAllByCategoryId(Integer categoryId, Pageable pageable);
}
