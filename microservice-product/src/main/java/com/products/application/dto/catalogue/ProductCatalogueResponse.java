
package com.products.application.dto.catalogue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class ProductCatalogueResponse {
    private String id;
    private String name;
    private String description;
    private Category category;
    private List<ProductSKU> SKUs;
    private List<ImageResponse> images;

    public static record Category(
            Long id, String name
    ){}

    public static record ImageResponse (
            UUID id,
            URL url
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
