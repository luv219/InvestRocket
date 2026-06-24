package com.investrocket.notification.dto;

import java.util.List;

public record NotificationSummaryResponse(
        long unreadCount,
        List<NotificationResponse> recentNotifications) {
}
