package com.products.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

// TODO: implement caffeine
@Entity @Table(name = "product_category")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductCategory {
    @Id
    private Integer id;

    @NotBlank @Length(max = 50)
    private String name;

    @Length(max = 255)
    private String description;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;
}



