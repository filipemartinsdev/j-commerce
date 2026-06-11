package com.notification.infra.persistence;

import com.notification.domain.entity.UserNotification;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserNotificationRepository implements PanacheRepositoryBase<UserNotification, UUID> {

    public PanacheQuery<UserNotification> findAllByUserId(UUID userId, int page, int size) {
        return this.find("userId = ?1", userId).page(page, size);
    }
}
