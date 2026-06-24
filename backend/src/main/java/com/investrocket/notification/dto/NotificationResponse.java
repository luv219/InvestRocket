package com.investrocket.notification.dto;

import java.time.Instant;
import java.util.UUID;

import com.investrocket.notification.Notification;
import com.investrocket.notification.NotificationCategory;
import com.investrocket.notification.NotificationType;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        NotificationCategory category,
        boolean isRead,
        String relatedEntityType,
        UUID relatedEntityId,
        Instant createdAt,
        Instant readAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getCategory(),
                notification.isRead(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
