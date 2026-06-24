package com.investrocket.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.order.OrderRepository;
import com.investrocket.portfolio.PortfolioService;
import com.investrocket.portfolio.dto.HoldingResponse;
import com.investrocket.portfolio.dto.PortfolioSummaryResponse;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private AnalyticsService analyticsService;
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = new User("Demo User", "demo@example.com", "hash");
        wallet = new Wallet(user);
        analyticsService = new AnalyticsService(
                snapshotRepository,
                portfolioService,
                walletRepository,
                tradeRepository,
                orderRepository,
                userRepository,
                Clock.fixed(
                        Instant.parse("2026-06-24T16:00:00Z"),
                        ZoneOffset.UTC));
    }

    @Test
    void calculatesPortfolioAnalyticsAndAllocation() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(portfolioService.getPortfolioSummary(user)).thenReturn(summary());
        when(portfolioService.getHoldings(user)).thenReturn(List.of(holding()));
        when(tradeRepository.findByUserOrderByExecutedAtDesc(user)).thenReturn(List.of());
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(snapshotRepository.findByUserOrderBySnapshotTimeAsc(user)).thenReturn(List.of());

        var analytics = analyticsService.getPortfolioAnalytics(user);

        assertThat(analytics.currentPortfolioValue()).isEqualByComparingTo("104250.00");
        assertThat(analytics.totalReturnPercent()).isEqualByComparingTo("4.25");
        assertThat(analytics.unrealizedProfitLoss()).isEqualByComparingTo("3000.00");
        assertThat(analytics.allocation()).hasSize(1);
        assertThat(analytics.allocation().getFirst().allocationPercent())
                .isEqualByComparingTo("100.00");
        assertThat(analytics.bestHolding().symbol()).isEqualTo("AAPL");
    }

    @Test
    void createsSnapshotWithZeroDailyChangeWhenNoPriorDayExists() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(portfolioService.getPortfolioSummary(user)).thenReturn(summary());
        when(tradeRepository.findByUserOrderByExecutedAtDesc(user)).thenReturn(List.of());
        when(snapshotRepository.findByUserOrderBySnapshotTimeDesc(user)).thenReturn(List.of());
        when(snapshotRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var snapshot = analyticsService.createSnapshot(user);

        assertThat(snapshot.date()).isEqualTo("2026-06-24");
        assertThat(snapshot.totalPortfolioValue()).isEqualByComparingTo("104250.00");
        assertThat(snapshot.dailyProfitLoss()).isEqualByComparingTo("0.00");
    }

    @Test
    void returnsZeroPercentagesForEmptyPortfolioInputs() {
        when(portfolioService.getHoldings(user)).thenReturn(List.of());

        assertThat(analyticsService.getAllocation(user)).isEmpty();
    }

    private PortfolioSummaryResponse summary() {
        return new PortfolioSummaryResponse(
                new BigDecimal("51250.00"),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("51250.00"),
                new BigDecimal("53000.00"),
                new BigDecimal("104250.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("6.00"),
                1);
    }

    private HoldingResponse holding() {
        return new HoldingResponse(
                "AAPL",
                "Apple Inc.",
                250,
                0,
                250,
                new BigDecimal("200.00"),
                new BigDecimal("212.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("53000.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("6.00"));
    }
}
