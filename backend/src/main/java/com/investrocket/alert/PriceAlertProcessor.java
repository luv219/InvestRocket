package com.investrocket.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.price-alert-processor.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PriceAlertProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceAlertProcessor.class);
    private final PriceAlertService priceAlertService;

    public PriceAlertProcessor(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }

    @Scheduled(fixedDelayString = "${app.price-alert-processor.interval-ms:15000}")
    public void processAlerts() {
        try {
            priceAlertService.checkAllActiveAlerts();
        } catch (RuntimeException exception) {
            LOGGER.warn("Price alert processor cycle failed: {}", exception.getMessage());
        }
    }
}
