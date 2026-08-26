package com.products.config;

import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.infra.persistence.ProductCategoryRepository;
import com.products.infra.persistence.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component @Profile("mock")
public class MockDatabaseSeeder {
    private final int BATCH_SIZE = 1000;
    private final long PRODUCT_COUNT = 100_000;
    private final long TOTAL_BATCHES = PRODUCT_COUNT / BATCH_SIZE;
    private final UUID ADMIN_ID = UUID.randomUUID();
    private final Faker faker = new Faker();

    @Bean
    public CommandLineRunner seed(ProductRepository productRepository, MongoTemplate mongoTemplate, ProductCategoryRepository productCategoryRepository){
        if (productRepository.count() != 0) return args -> {};

        log.info("Creating catalogue mock with {} products", PRODUCT_COUNT);

        return args -> {
            List<Product> productBatch = new ArrayList<>(BATCH_SIZE);

            var category = new ProductCategory();
            category.setId(1L);
            category.setName("mock");
            category.setCreatedBy(ADMIN_ID);
            productCategoryRepository.save(category);

            for (int i = 0; i < PRODUCT_COUNT; i++){
                var name = faker.commerce().productName();

                var SKU = new Product.ProductSKU();
                SKU.setSKU("sku"+i);
                SKU.setName(name);
                SKU.setBasePrice(new Product.ProductSKU.Price(
                        "Common",
                        new BigDecimal(faker.commerce().price(1, 1000))
                ));
                SKU.setCurrentPrice(new Product.ProductSKU.Price(
                        "Offer",
                        new BigDecimal(faker.commerce().price(1, SKU.getBasePrice().getValue().floatValue() - 10F))
                ));
                SKU.setCreatedBy(ADMIN_ID);

                var product = new Product();
                product.setName(name);
                product.setSKUs(List.of(SKU));
                product.setCategory(new Product.CategorySummary(category.getId(), category.getName()));
                product.setCreatedBy(ADMIN_ID);

                productBatch.add(product);

                if ((i + 1) % BATCH_SIZE == 0 || i == PRODUCT_COUNT - 1){
                    mongoTemplate.insertAll(productBatch);
                    productBatch.clear();

                    log.info("Inserted Batch {}/{}", (i / BATCH_SIZE) + 1, TOTAL_BATCHES);
                }
            }
        };
    }
}
