package com.notification.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "user_notification")
public class UserNotification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @NotNull
    @Column(name = "user_id")
    public UUID userId;

    @NotBlank
    public String title;

    @NotBlank
    public String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    public UserNotificationCategory category;

    @NotNull
    @Column(name = "viewed")
    public Boolean viewed = false;

    @CreationTimestamp
    @Column(name = "created_at")
    public Instant createdAt;
}
