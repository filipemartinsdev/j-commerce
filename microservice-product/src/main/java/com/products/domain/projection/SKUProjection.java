package com.products.domain.projection;

import com.products.domain.entity.Product;

import java.util.List;

public interface SKUProjection {
    List<Product.ProductSKU> getSKUs();
}
