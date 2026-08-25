
package com.products.application.dto.catalogue;

import java.math.BigDecimal;
import java.util.List;

public record ProductCatalogueResponse(
        String id,
        String name,
        String description,
        Category category,
        List<ProductSKU> SKUs
){

    public static record Category(
            Long id, String name
    ){}

    public static record ProductSKU (
           String SKU,
           String name,
           Long stock,
           Price currentPrice,
           Price basePrice,
           List<Attribute> attributes
    ){

        public static record Price (
                String label,
                BigDecimal value
        ){
        }

        public static record Attribute(String name, String value){}
    }
}
