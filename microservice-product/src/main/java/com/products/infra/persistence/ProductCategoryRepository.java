package com.products.infra.persistence;

import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
}
