package com.products.config;

import com.products.domain.entity.Product;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductSKUPrice;
import com.products.infra.persistence.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Configuration @Profile("mock")
public class MockDatabaseSeeder {
    private final ProductRepository productRepository;
    private final ProductSKUPriceRepository productSKUPriceRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final PriceTypeRepository priceTypeRepository;
    private final ProductSKURepository productSKURepository;

    public MockDatabaseSeeder(ProductRepository productRepository, ProductSKUPriceRepository productSKUPriceRepository, ProductCategoryRepository productCategoryRepository, PriceTypeRepository priceTypeRepository, ProductSKURepository productSKURepository) {
        this.productRepository = productRepository;
        this.productSKUPriceRepository = productSKUPriceRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.priceTypeRepository = priceTypeRepository;
        this.productSKURepository = productSKURepository;
    }

    @Bean @Transactional
    public CommandLineRunner seed(){
        if (productRepository.count() != 0)
            return args -> {};

        log.info("Creating product mocks...");

        return  args -> {
            for(int count = 0; count < 100; count++){
                createProductMock(count);
            }

            log.info("Product mocks created successfully");
        };
    }

    private void createProductMock(int count){
        var product = new Product();
        product.setName("product"+count);
        product.setCategory(productCategoryRepository.getReferenceById(1));
        productRepository.save(product);

        var sku = new ProductSKU();
        sku.setName("sku"+count);
        sku.setSKU("sku"+count);
        sku.setProduct(product);
        productSKURepository.save(sku);

        var price = new ProductSKUPrice();
        price.setPrice(BigDecimal.ONE);
        price.setStartAt(Instant.now());
        price.setProductSKU(sku);
        price.setPriceType(priceTypeRepository.getReferenceById(1));
        productSKUPriceRepository.save(price);

        log.info("Created product with id {}",product.getId());
    }
}
