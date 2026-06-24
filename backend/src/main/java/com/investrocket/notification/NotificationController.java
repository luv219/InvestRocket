package com.investrocket.notification;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.notification.dto.NotificationResponse;
import com.investrocket.notification.dto.NotificationSummaryResponse;
import com.investrocket.user.User;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public NotificationController(
            NotificationService notificationService,
            CurrentUserService currentUserService) {
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(Principal principal) {
        return ApiResponse.success("Notifications fetched successfully",
                notificationService.getMyNotifications(currentUser(principal)));
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> getUnread(Principal principal) {
        return ApiResponse.success("Unread notifications fetched successfully",
                notificationService.getUnreadNotifications(currentUser(principal)));
    }

    @GetMapping("/summary")
    public ApiResponse<NotificationSummaryResponse> getSummary(Principal principal) {
        return ApiResponse.success("Notification summary fetched successfully",
                notificationService.getNotificationSummary(currentUser(principal)));
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId,
            Principal principal) {
        return ApiResponse.success("Notification marked as read",
                notificationService.markAsRead(
                        notificationId, currentUser(principal)));
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(currentUser(principal));
        return ApiResponse.success("All notifications marked as read", null);
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID notificationId,
            Principal principal) {
        notificationService.deleteNotification(
                notificationId, currentUser(principal));
        return ApiResponse.success("Notification deleted", null);
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
