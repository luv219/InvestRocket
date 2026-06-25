package com.investrocket.alert;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.investrocket.audit.AuditLogService;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.notification.NotificationService;
import com.investrocket.user.User;
import com.investrocket.exception.PriceAlertNotFoundException;

class PriceAlertServiceTest {

    @Test
    void triggersAboveAlertAtTargetPrice() {
        PriceAlertRepository repository = Mockito.mock(PriceAlertRepository.class);
        NotificationService notifications = Mockito.mock(NotificationService.class);
        AuditLogService audit = Mockito.mock(AuditLogService.class);
        User user = Mockito.mock(User.class);
        PriceAlert alert = new PriceAlert(
                user, "AAPL", "Apple Inc.", new BigDecimal("200.0000"),
                PriceAlertCondition.ABOVE);
        when(repository.findByStatusAndSymbol(PriceAlertStatus.ACTIVE, "AAPL"))
                .thenReturn(List.of(alert));
        PriceAlertService service = new PriceAlertService(
                repository, Mockito.mock(MarketDataService.class), notifications, audit);

        service.checkAndTriggerAlertsForSymbol("aapl", new BigDecimal("200.0000"));

        verify(notifications).createNotification(
                Mockito.eq(user),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(),
                Mockito.any(),
                Mockito.anyString(),
                Mockito.any());
    }

    @Test
    void cannotCancelAnotherUsersAlert() {
        PriceAlertRepository repository = Mockito.mock(PriceAlertRepository.class);
        User user = Mockito.mock(User.class);
        UUID alertId = UUID.randomUUID();
        when(repository.findByIdAndUser(alertId, user)).thenReturn(Optional.empty());
        PriceAlertService service = new PriceAlertService(
                repository,
                Mockito.mock(MarketDataService.class),
                Mockito.mock(NotificationService.class),
                Mockito.mock(AuditLogService.class));

        assertThrows(
                PriceAlertNotFoundException.class,
                () -> service.cancelAlert(alertId, user));
    }
}
