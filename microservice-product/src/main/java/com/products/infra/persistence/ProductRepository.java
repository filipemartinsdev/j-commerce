package com.products.infra.persistence;

import com.products.domain.entity.Product;
import com.products.domain.projection.SKUProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Window<Product> findAllByOrderById(ScrollPosition scrollPosition, Limit limit);

    Window<Product> findAllByCategoryIdOrderById(Long categoryId, ScrollPosition scrollPosition, Limit limit);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(
            value = "{ id: ?0 }"
    )
    Optional<Product> findByIdWithLock(String id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(
            value = "{ 'SKUs.SKU': ?0 }"
    )
    Optional<Product> findBySKUWithLock(String SKU);

    @Query(
            value = "{ 'SKUs.SKU': ?0 }", fields = "{ SKUs: true, _id: false }"
    )
    Optional<SKUProjection> findSKU(String SKU);

    @Query(
            value = "{ 'SKUs.SKU': ?0 }"
    )
    Optional<Product> findBySKU(String SKU);

    @Query(
            value = "{ 'SKUs.currentPrice': { $exists: true, $ne: null }, _id: { $in: ?0 } }"
    )
    List<Product> findAllWithPriceById(List<String> ids);

    @Query(
            value = "{ 'SKUs.currentPrice': { $exists: true, $ne: null } }",
            sort = "{ _id: 1 }"
    )
    Window<Product> findAllWithPrice(ScrollPosition scrollPosition, Limit limit);

    @Query(
            value = "{ 'SKUs.currentPrice': { $exists: true, $ne: null }, category.id: ?0 }",
            sort = "{ _id: 1 }"
    )
    Window<Product> findAllWithPriceByCategory(Long categoryId, ScrollPosition scrollPosition, Limit limit);
}
