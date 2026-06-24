package com.investrocket.alert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.investrocket.alert.dto.CreatePriceAlertRequest;
import com.investrocket.alert.dto.PriceAlertResponse;
import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.PriceAlertNotFoundException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.notification.NotificationCategory;
import com.investrocket.notification.NotificationService;
import com.investrocket.notification.NotificationType;
import com.investrocket.user.User;

@Service
public class PriceAlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceAlertService.class);
    private final PriceAlertRepository priceAlertRepository;
    private final MarketDataService marketDataService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public PriceAlertService(
            PriceAlertRepository priceAlertRepository,
            MarketDataService marketDataService,
            NotificationService notificationService,
            AuditLogService auditLogService) {
        this.priceAlertRepository = priceAlertRepository;
        this.marketDataService = marketDataService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public PriceAlertResponse createAlert(CreatePriceAlertRequest request, User user) {
        String symbol = request.symbol().trim().toUpperCase(Locale.ROOT);
        StockQuoteResponse quote = marketDataService.getQuote(symbol);
        PriceAlert alert = priceAlertRepository.save(new PriceAlert(
                user, symbol, quote.companyName(),
                request.targetPrice().setScale(4, RoundingMode.HALF_UP),
                request.condition()));
        auditLogService.log(
                user, AuditCategory.SYSTEM, AuditAction.PRICE_ALERT_CREATED,
                "Price alert created for " + symbol);
        return PriceAlertResponse.from(alert);
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponse> getMyAlerts(User user) {
        return priceAlertRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(PriceAlertResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PriceAlertResponse> getActiveAlerts(User user) {
        return priceAlertRepository
                .findByUserAndStatusOrderByCreatedAtDesc(user, PriceAlertStatus.ACTIVE)
                .stream().map(PriceAlertResponse::from).toList();
    }

    @Transactional
    public PriceAlertResponse cancelAlert(UUID alertId, User user) {
        PriceAlert alert = priceAlertRepository.findByIdAndUser(alertId, user)
                .orElseThrow(PriceAlertNotFoundException::new);
        if (alert.getStatus() == PriceAlertStatus.ACTIVE) {
            alert.cancel();
            auditLogService.log(
                    user, AuditCategory.SYSTEM, AuditAction.PRICE_ALERT_CANCELLED,
                    "Price alert cancelled for " + alert.getSymbol());
        }
        return PriceAlertResponse.from(alert);
    }

    @Transactional
    public void checkAndTriggerAlertsForSymbol(String symbol, BigDecimal currentPrice) {
        String normalized = symbol.trim().toUpperCase(Locale.ROOT);
        priceAlertRepository.findByStatusAndSymbol(PriceAlertStatus.ACTIVE, normalized)
                .stream()
                .filter(alert -> alert.shouldTrigger(currentPrice))
                .forEach(alert -> trigger(alert, currentPrice));
    }

    @Transactional
    public void checkAllActiveAlerts() {
        priceAlertRepository.findByStatus(PriceAlertStatus.ACTIVE).stream()
                .map(PriceAlert::getSymbol)
                .distinct()
                .forEach(symbol -> {
                    try {
                        StockQuoteResponse quote = marketDataService.getQuote(symbol);
                        checkAndTriggerAlertsForSymbol(symbol, quote.currentPrice());
                    } catch (RuntimeException exception) {
                        LOGGER.warn(
                                "Unable to process price alerts for {}: {}",
                                symbol,
                                exception.getMessage());
                    }
                });
    }

    private void trigger(PriceAlert alert, BigDecimal price) {
        alert.trigger(price.setScale(4, RoundingMode.HALF_UP));
        notificationService.createNotification(
                alert.getUser(),
                "Price alert triggered",
                alert.getSymbol() + " reached " + price + " for your "
                        + alert.getCondition() + " alert.",
                NotificationType.SUCCESS,
                NotificationCategory.ALERT,
                "PRICE_ALERT",
                alert.getId());
        auditLogService.logWithinCurrentTransaction(
                alert.getUser(), AuditCategory.SYSTEM,
                AuditAction.PRICE_ALERT_TRIGGERED,
                "Price alert triggered for " + alert.getSymbol());
    }
}
