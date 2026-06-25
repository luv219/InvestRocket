package com.investrocket.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.analytics.dto.AllocationResponse;
import com.investrocket.analytics.dto.HoldingPerformanceResponse;
import com.investrocket.analytics.dto.PortfolioAnalyticsResponse;
import com.investrocket.analytics.dto.PortfolioPerformancePoint;
import com.investrocket.analytics.dto.TradingStatsResponse;
import com.investrocket.exception.WalletNotFoundException;
import com.investrocket.order.Order;
import com.investrocket.order.OrderRepository;
import com.investrocket.order.OrderStatus;
import com.investrocket.order.OrderSide;
import com.investrocket.portfolio.PortfolioService;
import com.investrocket.portfolio.dto.HoldingResponse;
import com.investrocket.portfolio.dto.PortfolioSummaryResponse;
import com.investrocket.trade.Trade;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@Service
public class AnalyticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PortfolioSnapshotRepository snapshotRepository;
    private final PortfolioService portfolioService;
    private final WalletRepository walletRepository;
    private final TradeRepository tradeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final AuditLogService auditLogService;

    @Autowired
    public AnalyticsService(
            PortfolioSnapshotRepository snapshotRepository,
            PortfolioService portfolioService,
            WalletRepository walletRepository,
            TradeRepository tradeRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this(
                snapshotRepository,
                portfolioService,
                walletRepository,
                tradeRepository,
                orderRepository,
                userRepository,
                Clock.systemUTC(),
                auditLogService);
    }

    AnalyticsService(
            PortfolioSnapshotRepository snapshotRepository,
            PortfolioService portfolioService,
            WalletRepository walletRepository,
            TradeRepository tradeRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            Clock clock) {
        this(
                snapshotRepository,
                portfolioService,
                walletRepository,
                tradeRepository,
                orderRepository,
                userRepository,
                clock,
                null);
    }

    private AnalyticsService(
            PortfolioSnapshotRepository snapshotRepository,
            PortfolioService portfolioService,
            WalletRepository walletRepository,
            TradeRepository tradeRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            Clock clock,
            AuditLogService auditLogService) {
        this.snapshotRepository = snapshotRepository;
        this.portfolioService = portfolioService;
        this.walletRepository = walletRepository;
        this.tradeRepository = tradeRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.clock = clock;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponse getPortfolioAnalytics(User currentUser) {
        Wallet wallet = requireWallet(currentUser);
        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(currentUser);
        List<HoldingPerformanceResponse> holdings = getHoldingPerformance(currentUser);
        BigDecimal realizedProfitLoss = realizedProfitLoss(currentUser);
        BigDecimal totalProfitLoss = realizedProfitLoss
                .add(summary.unrealizedProfitLoss())
                .setScale(2, RoundingMode.HALF_UP);

        return new PortfolioAnalyticsResponse(
                summary.totalPortfolioValue(),
                wallet.getInitialBalance(),
                summary.availableCash(),
                summary.reservedCash(),
                summary.holdingsValue(),
                summary.totalInvested(),
                realizedProfitLoss,
                summary.unrealizedProfitLoss(),
                totalProfitLoss,
                percentage(
                        summary.totalPortfolioValue().subtract(wallet.getInitialBalance()),
                        wallet.getInitialBalance()),
                holdings.stream()
                        .max(Comparator.comparing(
                                HoldingPerformanceResponse::unrealizedProfitLossPercent))
                        .orElse(null),
                holdings.stream()
                        .min(Comparator.comparing(
                                HoldingPerformanceResponse::unrealizedProfitLossPercent))
                        .orElse(null),
                allocation(holdings, summary.holdingsValue()),
                getPerformanceHistory(currentUser),
                getTradingStats(currentUser));
    }

    @Transactional(readOnly = true)
    public List<PortfolioPerformancePoint> getPerformanceHistory(User currentUser) {
        return snapshotRepository.findByUserOrderBySnapshotTimeAsc(currentUser)
                .stream()
                .map(this::toPerformancePoint)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocation(User currentUser) {
        List<HoldingPerformanceResponse> holdings = getHoldingPerformance(currentUser);
        BigDecimal holdingsValue = holdings.stream()
                .map(HoldingPerformanceResponse::currentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return allocation(holdings, holdingsValue);
    }

    @Transactional(readOnly = true)
    public List<HoldingPerformanceResponse> getHoldingPerformance(User currentUser) {
        return portfolioService.getHoldings(currentUser).stream()
                .map(this::toHoldingPerformance)
                .toList();
    }

    @Transactional(readOnly = true)
    public TradingStatsResponse getTradingStats(User currentUser) {
        List<Trade> trades = tradeRepository.findByUserOrderByExecutedAtDesc(currentUser);
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(currentUser);
        long buyTrades = trades.stream()
                .filter(trade -> trade.getSide() == OrderSide.BUY)
                .count();
        List<Trade> sellTrades = trades.stream()
                .filter(trade -> trade.getSide() == OrderSide.SELL)
                .toList();
        long winningSellTrades = sellTrades.stream()
                .filter(trade -> trade.getRealizedProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        long losingSellTrades = sellTrades.stream()
                .filter(trade -> trade.getRealizedProfitLoss().compareTo(BigDecimal.ZERO) < 0)
                .count();

        return new TradingStatsResponse(
                trades.size(),
                buyTrades,
                sellTrades.size(),
                orders.size(),
                countOrders(orders, OrderStatus.EXECUTED),
                countOrders(orders, OrderStatus.PENDING),
                countOrders(orders, OrderStatus.CANCELLED),
                trades.stream()
                        .map(Trade::getRealizedProfitLoss)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_UP),
                winningSellTrades,
                losingSellTrades,
                percentage(BigDecimal.valueOf(winningSellTrades), BigDecimal.valueOf(sellTrades.size())));
    }

    @Transactional
    public PortfolioPerformancePoint createSnapshot(User currentUser) {
        Wallet wallet = requireWallet(currentUser);
        PortfolioSummaryResponse summary = portfolioService.getPortfolioSummary(currentUser);
        Instant snapshotTime = clock.instant();
        LocalDate snapshotDate = LocalDate.ofInstant(snapshotTime, ZoneOffset.UTC);
        BigDecimal previousValue = snapshotRepository
                .findByUserOrderBySnapshotTimeDesc(currentUser)
                .stream()
                .filter(snapshot -> snapshot.getSnapshotDate().isBefore(snapshotDate))
                .map(PortfolioSnapshot::getTotalPortfolioValue)
                .findFirst()
                .orElse(summary.totalPortfolioValue());
        BigDecimal dailyProfitLoss = summary.totalPortfolioValue()
                .subtract(previousValue)
                .setScale(2, RoundingMode.HALF_UP);

        PortfolioSnapshot snapshot = new PortfolioSnapshot(
                currentUser,
                summary.availableCash(),
                summary.reservedCash(),
                summary.holdingsValue(),
                summary.totalPortfolioValue(),
                summary.totalInvested(),
                summary.unrealizedProfitLoss(),
                realizedProfitLoss(currentUser),
                dailyProfitLoss,
                percentage(dailyProfitLoss, previousValue),
                percentage(
                        summary.totalPortfolioValue().subtract(wallet.getInitialBalance()),
                        wallet.getInitialBalance()),
                snapshotDate,
                snapshotTime);
        PortfolioSnapshot savedSnapshot = snapshotRepository.save(snapshot);
        if (auditLogService != null) {
            auditLogService.log(
                    currentUser,
                    AuditCategory.ANALYTICS,
                    AuditAction.SNAPSHOT_CREATED,
                    "Portfolio analytics snapshot created");
        }
        return toPerformancePoint(savedSnapshot);
    }

    public void createSnapshotsForAllUsers() {
        for (User user : userRepository.findByEnabledTrue()) {
            try {
                createSnapshot(user);
            } catch (RuntimeException exception) {
                LOGGER.warn("Unable to create portfolio snapshot for user {}", user.getId(), exception);
            }
        }
    }

    private List<AllocationResponse> allocation(
            List<HoldingPerformanceResponse> holdings,
            BigDecimal holdingsValue) {
        return holdings.stream()
                .map(holding -> new AllocationResponse(
                        holding.symbol(),
                        holding.companyName(),
                        holding.currentValue(),
                        percentage(holding.currentValue(), holdingsValue)))
                .toList();
    }

    private HoldingPerformanceResponse toHoldingPerformance(HoldingResponse holding) {
        return new HoldingPerformanceResponse(
                holding.symbol(),
                holding.companyName(),
                holding.quantity(),
                holding.averageBuyPrice(),
                holding.currentPrice(),
                holding.currentValue(),
                holding.totalInvested(),
                holding.unrealizedProfitLoss(),
                holding.unrealizedProfitLossPercent());
    }

    private PortfolioPerformancePoint toPerformancePoint(PortfolioSnapshot snapshot) {
        return new PortfolioPerformancePoint(
                snapshot.getSnapshotDate(),
                snapshot.getSnapshotTime(),
                snapshot.getTotalPortfolioValue(),
                snapshot.getCashBalance().add(snapshot.getReservedCash()),
                snapshot.getHoldingsValue(),
                snapshot.getDailyProfitLoss(),
                snapshot.getDailyProfitLossPercent());
    }

    private BigDecimal realizedProfitLoss(User user) {
        return tradeRepository.findByUserOrderByExecutedAtDesc(user).stream()
                .map(Trade::getRealizedProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private long countOrders(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(order -> order.getStatus() == status).count();
    }

    private Wallet requireWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(WalletNotFoundException::new);
    }

    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return numerator.multiply(ONE_HUNDRED)
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
