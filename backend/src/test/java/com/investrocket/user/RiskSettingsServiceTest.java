package com.investrocket.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.RiskControlViolationException;
import com.investrocket.order.OrderRepository;
import com.investrocket.order.OrderSide;
import com.investrocket.order.OrderType;
import com.investrocket.order.dto.CreateOrderRequest;
import com.investrocket.user.dto.UpdateRiskSettingsRequest;

@ExtendWith(MockitoExtension.class)
class RiskSettingsServiceTest {

    @Mock
    private RiskSettingsRepository riskSettingsRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuditLogService auditLogService;

    private RiskSettingsService service;
    private User user;
    private UserRiskSettings settings;

    @BeforeEach
    void setUp() {
        service = new RiskSettingsService(
                riskSettingsRepository,
                orderRepository,
                auditLogService);
        user = new User("Demo User", "demo@example.com", "hash");
        settings = new UserRiskSettings(user);
    }

    @Test
    void lazilyCreatesDefaultSettings() {
        when(riskSettingsRepository.findByUser(user)).thenReturn(Optional.empty());
        when(riskSettingsRepository.save(any(UserRiskSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.getRiskSettings(user);

        assertThat(response.maxOrderValue()).isEqualByComparingTo("25000.00");
        assertThat(response.maxDailyTrades()).isEqualTo(50);
    }

    @Test
    void rejectsOrderAboveMaximumValue() {
        when(riskSettingsRepository.findByUser(user)).thenReturn(Optional.of(settings));

        assertThatThrownBy(() -> service.validateOrderAgainstRiskControls(
                request(OrderType.MARKET),
                user,
                new BigDecimal("25000.01")))
                .isInstanceOf(RiskControlViolationException.class)
                .hasMessageContaining("maximum order value");
    }

    @Test
    void rejectsDisabledLimitOrders() {
        settings.update(new BigDecimal("50000.00"), 50, true, false);
        when(riskSettingsRepository.findByUser(user)).thenReturn(Optional.of(settings));

        assertThatThrownBy(() -> service.validateOrderAgainstRiskControls(
                request(OrderType.LIMIT),
                user,
                new BigDecimal("1000.00")))
                .isInstanceOf(RiskControlViolationException.class)
                .hasMessageContaining("Limit orders are disabled");
    }

    @Test
    void updatesSettingsWithinBounds() {
        when(riskSettingsRepository.findByUser(user)).thenReturn(Optional.of(settings));

        var response = service.updateRiskSettings(
                new UpdateRiskSettingsRequest(
                        new BigDecimal("10000.00"),
                        20,
                        false,
                        true),
                user);

        assertThat(response.maxOrderValue()).isEqualByComparingTo("10000.00");
        assertThat(response.allowStopLossOrders()).isFalse();
    }

    private CreateOrderRequest request(OrderType type) {
        return new CreateOrderRequest(
                "AAPL",
                OrderSide.BUY,
                type,
                1,
                type == OrderType.LIMIT ? new BigDecimal("100.00") : null,
                null);
    }
}
