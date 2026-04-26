package com.notification.application.service.mapper;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.domain.entity.UserNotification;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationMapper {
    public UserNotificationResponse toResponse(UserNotification userNotification){
        return new UserNotificationResponse(
                userNotification.getId(),
                userNotification.getTitle(),
                userNotification.getDescription(),
                userNotification.getCategory().getName(),
                userNotification.getIsRead(),
                userNotification.getCreatedAt()
        );
    }
}
