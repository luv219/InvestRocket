package com.investrocket.notification;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
}
