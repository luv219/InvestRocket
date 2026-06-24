package com.investrocket.alert.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.alert.PriceAlert;
import com.investrocket.alert.PriceAlertCondition;
import com.investrocket.alert.PriceAlertStatus;

public record PriceAlertResponse(
        UUID id,
        String symbol,
        String companyName,
        BigDecimal targetPrice,
        PriceAlertCondition condition,
        PriceAlertStatus status,
        BigDecimal triggeredPrice,
        Instant triggeredAt,
        Instant createdAt,
        Instant updatedAt) {

    public static PriceAlertResponse from(PriceAlert alert) {
        return new PriceAlertResponse(
                alert.getId(), alert.getSymbol(), alert.getCompanyName(),
                alert.getTargetPrice(), alert.getCondition(), alert.getStatus(),
                alert.getTriggeredPrice(), alert.getTriggeredAt(),
                alert.getCreatedAt(), alert.getUpdatedAt());
    }
}
