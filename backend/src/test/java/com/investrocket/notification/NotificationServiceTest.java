package com.investrocket.notification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.investrocket.user.User;

class NotificationServiceTest {

    @Test
    void returnsOnlyUnreadNotificationsForCurrentUser() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        User user = Mockito.mock(User.class);
        Notification notification = Mockito.mock(Notification.class);
        when(repository.findByUserAndReadOrderByCreatedAtDesc(user, false))
                .thenReturn(List.of(notification));

        new NotificationService(repository).getUnreadNotifications(user);

        verify(repository).findByUserAndReadOrderByCreatedAtDesc(user, false);
    }

    @Test
    void marksOwnedNotificationAsRead() {
        NotificationRepository repository = Mockito.mock(NotificationRepository.class);
        User user = Mockito.mock(User.class);
        Notification notification = new Notification(
                user,
                "Order executed",
                "Your virtual order executed.",
                NotificationType.SUCCESS,
                NotificationCategory.ORDER,
                null,
                null);
        UUID notificationId = notification.getId();
        when(repository.findByIdAndUser(notificationId, user))
                .thenReturn(Optional.of(notification));

        var response = new NotificationService(repository)
                .markAsRead(notificationId, user);

        assertThat(response.isRead()).isTrue();
        assertThat(response.readAt()).isNotNull();
    }
}
