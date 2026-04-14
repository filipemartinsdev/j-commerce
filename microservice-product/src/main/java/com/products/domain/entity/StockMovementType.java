package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity @Table(name = "stock_movement_type")
@Data @NoArgsConstructor @AllArgsConstructor
public class StockMovementType {
    @Id
    private Integer id;

    @NotBlank @Length(max = 50)
    private String name;

    @Getter
    public static enum Value {
        ENTRY(1, "ENTRY"),
        SALE(2, "SALE"),
        REFOUND(3, "REFOUND"),
        ADJUST(4, "ADJUST"),
        OTHER(5, "OTHER");

        private final Integer id;
        private final String name;

        Value(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
