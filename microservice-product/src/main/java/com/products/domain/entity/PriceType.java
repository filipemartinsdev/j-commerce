package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "product_price_type")
public class PriceType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @Getter
    public static enum Value {
        COMMON(1, "common"),
        OFFER(2, "offer"),
        BLACK_FRIDAY(3, "black_friday");

        private final int id;
        private final String name;

        Value(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Value fromId(int id) {
            return switch (id){
                case 1 -> COMMON;
                case 2 -> OFFER;
                case 3 -> BLACK_FRIDAY;
                default -> null;
            };
        }
    }
}