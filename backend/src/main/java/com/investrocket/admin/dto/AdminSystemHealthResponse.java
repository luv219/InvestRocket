package com.investrocket.admin.dto;

import java.time.Instant;

public record AdminSystemHealthResponse(
        String backendStatus,
        String databaseStatus,
        String marketDataProvider,
        boolean livePriceStreamEnabled,
        boolean pendingOrderProcessorEnabled,
        boolean portfolioSnapshotEnabled,
        Instant currentTime,
        String activeProfile,
        String applicationName,
        String version) {
}
