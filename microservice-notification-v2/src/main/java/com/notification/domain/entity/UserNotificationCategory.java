package com.notification.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity @Table(name = "user_notification_category")
public class UserNotificationCategory extends PanacheEntity {
    @NotBlank
    public String name;

    public static enum Value {
        RECOMMENDATION(1, "RECOMMENDATION"),
        WARNING(2, "WARNING"),
        PURCHASE(3, "PURCHASE"),
        DELIVERY(4, "DELIVERY"),
        OTHER(5, "OTHER");

        public final long id;
        public final String name;

        Value(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
