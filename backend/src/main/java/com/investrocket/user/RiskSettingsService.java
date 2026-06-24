package com.investrocket.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.RiskControlViolationException;
import com.investrocket.order.OrderRepository;
import com.investrocket.order.OrderType;
import com.investrocket.order.dto.CreateOrderRequest;
import com.investrocket.user.dto.RiskSettingsResponse;
import com.investrocket.user.dto.UpdateRiskSettingsRequest;

@Service
public class RiskSettingsService {

    private final RiskSettingsRepository riskSettingsRepository;
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;

    public RiskSettingsService(
            RiskSettingsRepository riskSettingsRepository,
            OrderRepository orderRepository,
            AuditLogService auditLogService) {
        this.riskSettingsRepository = riskSettingsRepository;
        this.orderRepository = orderRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UserRiskSettings createDefaults(User user) {
        return riskSettingsRepository.findByUser(user)
                .orElseGet(() -> riskSettingsRepository.save(new UserRiskSettings(user)));
    }

    @Transactional
    public RiskSettingsResponse getRiskSettings(User currentUser) {
        return RiskSettingsResponse.from(createDefaults(currentUser));
    }

    @Transactional
    public RiskSettingsResponse updateRiskSettings(
            UpdateRiskSettingsRequest request,
            User currentUser) {
        UserRiskSettings settings = createDefaults(currentUser);
        settings.update(
                request.maxOrderValue().setScale(2, RoundingMode.HALF_UP),
                request.maxDailyTrades(),
                request.allowStopLossOrders(),
                request.allowLimitOrders());
        auditLogService.log(
                currentUser,
                AuditCategory.RISK,
                AuditAction.RISK_SETTINGS_UPDATED,
                "Trading risk settings updated");
        return RiskSettingsResponse.from(settings);
    }

    @Transactional
    public void validateOrderAgainstRiskControls(
            CreateOrderRequest request,
            User currentUser,
            BigDecimal estimatedOrderValue) {
        UserRiskSettings settings = createDefaults(currentUser);
        if (estimatedOrderValue.compareTo(settings.getMaxOrderValue()) > 0) {
            throw new RiskControlViolationException(
                    "Order value exceeds your maximum order value limit.");
        }
        if (request.orderType() == OrderType.LIMIT && !settings.isAllowLimitOrders()) {
            throw new RiskControlViolationException(
                    "Limit orders are disabled in your risk settings.");
        }
        if (request.orderType() == OrderType.STOP_LOSS
                && !settings.isAllowStopLossOrders()) {
            throw new RiskControlViolationException(
                    "Stop-loss orders are disabled in your risk settings.");
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant start = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        if (orderRepository.countByUserAndCreatedAtBetween(currentUser, start, end)
                >= settings.getMaxDailyTrades()) {
            throw new RiskControlViolationException("Daily trade limit reached.");
        }
    }
}
