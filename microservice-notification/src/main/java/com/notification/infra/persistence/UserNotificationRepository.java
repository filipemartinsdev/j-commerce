package com.notification.infra.persistence;

import com.notification.domain.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {
    Page<UserNotification> findAllByUserId(UUID userId, Pageable pageable);
}
