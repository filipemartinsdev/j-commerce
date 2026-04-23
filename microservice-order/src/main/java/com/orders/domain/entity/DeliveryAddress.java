package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "delivery_address")
@Data @NoArgsConstructor @AllArgsConstructor
public class DeliveryAddress {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id")
    private UUID userId;

    @NotBlank
    @Length(max = 8)
    @Column(name = "zip_code")
    private String zipCode;

    @NotBlank
    @Length(max = 255)
    @Column(name = "street")
    private String street;

    @NotBlank
    @Length(max = 20)
    @Column(name = "number")
    private String number;

    @Length(max = 255)
    @Column(name = "complement")
    private String complement;

    @NotBlank
    @Length(max = 50)
    @Column(name = "neighborhood")
    private String neighborhood;

    @NotBlank
    @Length(max = 100)
    @Column(name = "city")
    private String city;

    @NotBlank
    @Length(max = 2)
    @Column(name = "state")
    private String state;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;
}
