package com.investrocket.analytics;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotSchedulerTest {

    @Mock
    private AnalyticsService analyticsService;

    @Test
    void delegatesSnapshotCreationForAllUsers() {
        PortfolioSnapshotScheduler scheduler =
                new PortfolioSnapshotScheduler(analyticsService);

        scheduler.createSnapshots();

        verify(analyticsService).createSnapshotsForAllUsers();
    }
}
