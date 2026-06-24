package com.investrocket.notification;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.investrocket.exception.NotificationNotFoundException;
import com.investrocket.notification.dto.NotificationResponse;
import com.investrocket.notification.dto.NotificationSummaryResponse;
import com.investrocket.user.User;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final TransactionTemplate transactionTemplate;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.transactionTemplate = null;
    }

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            PlatformTransactionManager transactionManager) {
        this.notificationRepository = notificationRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void createNotification(
            User user,
            String title,
            String message,
            NotificationType type,
            NotificationCategory category) {
        createNotification(user, title, message, type, category, null, null);
    }

    public void createNotification(
            User user,
            String title,
            String message,
            NotificationType type,
            NotificationCategory category,
            String relatedEntityType,
            UUID relatedEntityId) {
        try {
            Notification notification = new Notification(
                    user, title, message, type, category,
                    relatedEntityType, relatedEntityId);
            if (transactionTemplate == null) {
                notificationRepository.save(notification);
            } else {
                transactionTemplate.executeWithoutResult(
                        status -> notificationRepository.saveAndFlush(notification));
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to create notification for user {}", user.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotificationSummary(User user) {
        List<NotificationResponse> recent = notificationRepository
                .findByUserOrderByCreatedAtDesc(user).stream()
                .limit(5)
                .map(NotificationResponse::from)
                .toList();
        return new NotificationSummaryResponse(
                notificationRepository.countByUserAndRead(user, false),
                recent);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(NotificationNotFoundException::new);
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false)
                .forEach(Notification::markAsRead);
    }

    @Transactional
    public void deleteNotification(UUID notificationId, User user) {
        if (notificationRepository.deleteByUserAndId(user, notificationId) == 0) {
            throw new NotificationNotFoundException();
        }
    }
}
