package com.products.infra.persistence;

import com.products.model.entity.ProductCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductCategoryRepository extends MongoRepository<ProductCategory, Long> {

}
