package com.products.model.projection;

import com.products.model.entity.Product;

import java.util.List;

public interface SKUProjection {
    List<Product.ProductSKU> getSKUs();
}
