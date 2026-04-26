package com.notification.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "user_notification_category")
public class UserNotificationCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    private String name;

    @Getter
    public static enum Value {
        RECOMMENDATION(1, "RECOMMENDATION"),
        WARNING(2, "WARNING"),
        PURCHASE(3, "PURCHASE"),
        DELIVERY(4, "DELIVERY"),
        OTHER(5, "OTHER");

        private final int id;
        private final String name;

        Value(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}