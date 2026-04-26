package com.notification.infra.persistence;

import com.notification.domain.entity.UserNotificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationCategoryRepository extends JpaRepository<UserNotificationCategory, Integer> {
}
