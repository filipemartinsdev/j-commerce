package com.identity.security.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "role")
@Data @NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @Getter
    public static enum Value {
        USER(1, "USER"),
        ADMIN(2, "ADMIN"),
        STOCK_MANAGER(3, "STOCK_MANAGER"),
        DRIVER(4, "DRIVER");

        private final int id;
        private final String name;

        Value(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Value fromId(int id) {
            return switch(id){
                case 1 -> USER;
                case 2 -> ADMIN;
                case 3 -> STOCK_MANAGER;
                case 4 -> DRIVER;
                default -> null;
            };
        }
    }
}
