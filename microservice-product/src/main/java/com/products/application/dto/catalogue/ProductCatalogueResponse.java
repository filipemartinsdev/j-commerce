
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
           Price currentPrice
    ){

        public static record Price (
                String type,
                BigDecimal value
        ){
        }
    }
}
