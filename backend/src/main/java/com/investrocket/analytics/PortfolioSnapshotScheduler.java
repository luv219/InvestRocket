package com.investrocket.analytics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.portfolio-snapshot",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PortfolioSnapshotScheduler {

    private final AnalyticsService analyticsService;

    public PortfolioSnapshotScheduler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Scheduled(fixedDelayString = "${app.portfolio-snapshot.interval-ms:300000}")
    public void createSnapshots() {
        analyticsService.createSnapshotsForAllUsers();
    }
}
