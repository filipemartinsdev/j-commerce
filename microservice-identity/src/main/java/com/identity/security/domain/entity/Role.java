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
        USER(1),
        ADMIN(2),
        STOCK_MANAGER(3),
        DRIVER(4),
        LOGISTICS(5);

        private final int id;

        Value(int id) {
            this.id = id;
        }

        public static Value fromId(int id) {
            return switch(id){
                case 1 -> USER;
                case 2 -> ADMIN;
                case 3 -> STOCK_MANAGER;
                case 4 -> DRIVER;
                case 5 -> LOGISTICS;
                default -> null;
            };
        }
    }
}
