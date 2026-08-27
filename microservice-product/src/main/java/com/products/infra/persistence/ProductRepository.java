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
            value = "{ 'SKUs.SKU': ?0 , 'SKUs.currentPrice': { $ne: null } }", fields = "{ SKUs: true, id: false }"
    )
    Optional<SKUProjection> findSKUWithPrice(String SKU);

    @Query(
            value = "{ 'SKUs.SKU': ?0 }"
    )
    Optional<Product> findBySKU(String SKU);

    @Query(
            value = "{ 'SKUs.currentPrice': { $exists: true, $ne: null }, id: { $in: ?0 } }"
    )
    List<Product> findAllWithPriceById(List<String> ids);

    @Query(
            value = "{ 'SKUs.currentPrice': { $exists: true, $ne: null }, 'SKUs.basePrice': { $exists: true, $ne: null } }",
            sort = "{ id: 1 }"
    )
    Window<Product> findAllWithPrice(ScrollPosition scrollPosition, Limit limit);

    @Query(
            value = "{ 'SKUs.currentPrice': { $exists: true, $ne: null }, 'SKUs.basePrice': { $exists: true, $ne: null }, 'category.id': ?0 }",
            sort = "{ id: 1 }"
    )
    Window<Product> findAllWithPriceByCategory(Long categoryId, ScrollPosition scrollPosition, Limit limit);
}
