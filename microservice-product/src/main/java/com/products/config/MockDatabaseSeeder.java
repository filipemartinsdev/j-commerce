package com.products.config;

import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductSKUPrice;
import com.products.infra.persistence.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Configuration @Profile("mock")
public class MockDatabaseSeeder {
    private final int BATCH_SIZE = 1000;
    private final long PRODUCT_COUNT = 100_000;
    private final UUID ADMIN_ID = UUID.randomUUID();

    private final Faker faker;
    private final JdbcTemplate jdbcTemplate;
    private final ProductRepository productRepository;
    private final ProductSKUPriceRepository productSKUPriceRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final PriceTypeRepository priceTypeRepository;
    private final ProductSKURepository productSKURepository;

    public MockDatabaseSeeder(JdbcTemplate jdbcTemplate, ProductRepository productRepository, ProductSKUPriceRepository productSKUPriceRepository, ProductCategoryRepository productCategoryRepository, PriceTypeRepository priceTypeRepository, ProductSKURepository productSKURepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.faker = new Faker();
        this.productRepository = productRepository;
        this.productSKUPriceRepository = productSKUPriceRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.productSKURepository = productSKURepository;
    }

    @Bean
    public CommandLineRunner seed(){
        if (productRepository.count() != 0) return args -> {};

        log.info("Creating catalogue mock with {} products", PRODUCT_COUNT);

        return args -> {
            String productSQL = """
            INSERT INTO product (id, name, description, category_id, embedding, created_at, updated_at, is_active)
            VALUES (?, ?, NULL, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE)
            """;

            String skuSQL = """
            INSERT INTO product_sku (id, product_id, sku, name, created_at, updated_at, is_active)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE)
            """;

            String priceSQL = """
            INSERT INTO product_sku_price (id, product_sku_id, price, price_type_id, start_at, end_at, created_at, is_active)
            VALUES (?, ?, ?, 1, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, TRUE)
            """;

            String stockSQL = """
            INSERT INTO product_stock (id, product_sku_id, units, created_at, created_by, updated_at, is_active)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, TRUE)
            """;

            var productArgs = new ArrayList<Object[]>();
            var skuArgs = new ArrayList<Object[]>();
            var priceArgs = new ArrayList<Object[]>();
            var stockArgs = new ArrayList<Object[]>();

            for (int i = 0; i < PRODUCT_COUNT; i++){
                var productId = UUID.randomUUID();
                var skuId = UUID.randomUUID();
                var priceId = UUID.randomUUID();
                var stockId = UUID.randomUUID();

                String name = faker.commerce().productName();
                int units = ThreadLocalRandom.current().nextInt(1, 1000);

                productArgs.add(new Object[]{productId, name});
                skuArgs.add(new Object[]{skuId, productId, "SKU-"+i, name});
                priceArgs.add(new Object[]{priceId, skuId, new BigDecimal(faker.commerce().price())});
                stockArgs.add(new Object[]{stockId, skuId, units, ADMIN_ID});

                long totalBatches = PRODUCT_COUNT / BATCH_SIZE;

                if ((i + 1) % BATCH_SIZE == 0 || i == PRODUCT_COUNT - 1){
                    jdbcTemplate.batchUpdate(productSQL, productArgs);
                    jdbcTemplate.batchUpdate(skuSQL, skuArgs);
                    jdbcTemplate.batchUpdate(priceSQL, priceArgs);
                    jdbcTemplate.batchUpdate(stockSQL, stockArgs);

                    productArgs.clear();
                    skuArgs.clear();
                    priceArgs.clear();
                    stockArgs.clear();

                    log.info("Inserted Batch {}/{}", (i / BATCH_SIZE) + 1, totalBatches);
                }
            }

            log.info("Created {} product mocks successfully", PRODUCT_COUNT);
        };
    }
}
