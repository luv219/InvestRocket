package com.investrocket.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.admin.dto.AdminAuditLogResponse;
import com.investrocket.admin.dto.AdminDashboardStatsResponse;
import com.investrocket.admin.dto.AdminMarketDataStatusResponse;
import com.investrocket.admin.dto.AdminOrderResponse;
import com.investrocket.admin.dto.AdminSystemHealthResponse;
import com.investrocket.admin.dto.AdminTradeResponse;
import com.investrocket.admin.dto.AdminTradingStatsResponse;
import com.investrocket.admin.dto.SymbolCountResponse;
import com.investrocket.admin.dto.UserMetricResponse;
import com.investrocket.analytics.PortfolioSnapshotRepository;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLog;
import com.investrocket.audit.AuditLogRepository;
import com.investrocket.exception.InvalidAuditCategoryException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.order.Order;
import com.investrocket.order.OrderRepository;
import com.investrocket.order.OrderSide;
import com.investrocket.order.OrderStatus;
import com.investrocket.order.OrderType;
import com.investrocket.portfolio.PortfolioService;
import com.investrocket.trade.Trade;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.Role;
import com.investrocket.user.User;
import com.investrocket.user.UserRepository;
import com.investrocket.wallet.WalletRepository;
import com.investrocket.watchlist.WatchlistRepository;

@Service
public class AdminMonitoringService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final WatchlistRepository watchlistRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final AuditLogRepository auditLogRepository;
    private final PortfolioService portfolioService;
    private final MarketDataService marketDataService;
    private final Environment environment;
    private final String provider;
    private final boolean livePriceEnabled;
    private final boolean pendingProcessorEnabled;
    private final boolean snapshotEnabled;
    private final String applicationName;
    private final String version;

    public AdminMonitoringService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            OrderRepository orderRepository,
            TradeRepository tradeRepository,
            WatchlistRepository watchlistRepository,
            PortfolioSnapshotRepository snapshotRepository,
            AuditLogRepository auditLogRepository,
            PortfolioService portfolioService,
            MarketDataService marketDataService,
            Environment environment,
            @Value("${app.financial-api.provider:mock}") String provider,
            @Value("${app.live-price-stream.enabled:true}") boolean livePriceEnabled,
            @Value("${app.pending-order-processor.enabled:true}") boolean pendingProcessorEnabled,
            @Value("${app.portfolio-snapshot.enabled:true}") boolean snapshotEnabled,
            @Value("${spring.application.name:invest-rocket-backend}") String applicationName,
            @Value("${info.app.version:0.0.1-SNAPSHOT}") String version) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.watchlistRepository = watchlistRepository;
        this.snapshotRepository = snapshotRepository;
        this.auditLogRepository = auditLogRepository;
        this.portfolioService = portfolioService;
        this.marketDataService = marketDataService;
        this.environment = environment;
        this.provider = provider;
        this.livePriceEnabled = livePriceEnabled;
        this.pendingProcessorEnabled = pendingProcessorEnabled;
        this.snapshotEnabled = snapshotEnabled;
        this.applicationName = applicationName;
        this.version = version;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        BigDecimal totalCash = walletRepository.findAll().stream()
                .map(wallet -> wallet.getCashBalance().add(wallet.getReservedBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal holdingsValue = BigDecimal.ZERO;
        BigDecimal unrealized = BigDecimal.ZERO;
        for (User user : userRepository.findAll()) {
            try {
                var summary = portfolioService.getPortfolioSummary(user);
                holdingsValue = holdingsValue.add(summary.holdingsValue());
                unrealized = unrealized.add(summary.unrealizedProfitLoss());
            } catch (RuntimeException ignored) {
            }
        }
        BigDecimal realized = tradeRepository.findAll().stream()
                .map(Trade::getRealizedProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AdminDashboardStatsResponse(
                userRepository.count(),
                userRepository.countByEnabledTrue(),
                userRepository.countByEnabledFalse(),
                userRepository.countByRole(Role.ADMIN),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.EXECUTED),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                orderRepository.countByStatus(OrderStatus.REJECTED),
                tradeRepository.count(),
                watchlistRepository.count(),
                snapshotRepository.count(),
                money(totalCash),
                money(holdingsValue),
                money(totalCash.add(holdingsValue)),
                money(realized),
                money(unrealized));
    }

    @Transactional(readOnly = true)
    public AdminTradingStatsResponse getTradingStats() {
        List<Trade> trades = tradeRepository.findAll();
        Map<String, Long> symbolCounts = new HashMap<>();
        Map<String, Long> userTradeCounts = new HashMap<>();
        for (Trade trade : trades) {
            symbolCounts.merge(trade.getSymbol(), 1L, Long::sum);
            userTradeCounts.merge(trade.getUser().getEmail(), 1L, Long::sum);
        }
        List<UserMetricResponse> portfolioMetrics = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            BigDecimal value;
            try {
                value = portfolioService.getPortfolioSummary(user).totalPortfolioValue();
            } catch (RuntimeException exception) {
                value = BigDecimal.ZERO;
            }
            portfolioMetrics.add(new UserMetricResponse(
                    user.getEmail(),
                    userTradeCounts.getOrDefault(user.getEmail(), 0L),
                    money(value)));
        }
        return new AdminTradingStatsResponse(
                orderRepository.countBySide(OrderSide.BUY),
                orderRepository.countBySide(OrderSide.SELL),
                orderRepository.countByOrderType(OrderType.MARKET),
                orderRepository.countByOrderType(OrderType.LIMIT),
                orderRepository.countByOrderType(OrderType.STOP_LOSS),
                symbolCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(10)
                        .map(entry -> new SymbolCountResponse(
                                entry.getKey(),
                                entry.getValue()))
                        .toList(),
                portfolioMetrics.stream()
                        .sorted(Comparator.comparingLong(
                                UserMetricResponse::tradeCount).reversed())
                        .limit(10)
                        .toList(),
                portfolioMetrics.stream()
                        .sorted(Comparator.comparing(
                                UserMetricResponse::portfolioValue).reversed())
                        .limit(10)
                        .toList(),
                tradeRepository.findTop20ByOrderByExecutedAtDesc().stream()
                        .map(AdminTradeResponse::from)
                        .toList(),
                orderRepository.findTop20ByOrderByCreatedAtDesc().stream()
                        .map(AdminOrderResponse::from)
                        .toList());
    }

    @Transactional(readOnly = true)
    public AdminSystemHealthResponse getSystemHealth() {
        String databaseStatus;
        try {
            userRepository.count();
            databaseStatus = "UP";
        } catch (RuntimeException exception) {
            databaseStatus = "DOWN";
        }
        String[] profiles = environment.getActiveProfiles();
        return new AdminSystemHealthResponse(
                "UP",
                databaseStatus,
                provider,
                livePriceEnabled,
                pendingProcessorEnabled,
                snapshotEnabled,
                Instant.now(),
                profiles.length == 0
                        ? String.join(",", environment.getDefaultProfiles())
                        : String.join(",", profiles),
                applicationName,
                version);
    }

    @Transactional(readOnly = true)
    public List<AdminAuditLogResponse> getRecentAuditLogs(
            String category,
            String userEmail) {
        List<AuditLog> logs;
        if (userEmail != null && !userEmail.isBlank()) {
            User user = userRepository.findByEmail(userEmail.trim().toLowerCase(Locale.ROOT))
                    .orElse(null);
            logs = user == null
                    ? List.of()
                    : auditLogRepository.findTop100ByUserOrderByCreatedAtDesc(user);
            if (category != null && !category.isBlank()) {
                AuditCategory parsedCategory = parseCategory(category);
                logs = logs.stream()
                        .filter(log -> log.getCategory() == parsedCategory)
                        .toList();
            }
        } else if (category != null && !category.isBlank()) {
            logs = auditLogRepository.findTop100ByCategoryOrderByCreatedAtDesc(
                    parseCategory(category));
        } else {
            logs = auditLogRepository.findTop100ByOrderByCreatedAtDesc();
        }
        return logs.stream().map(AdminAuditLogResponse::from).toList();
    }

    public AdminMarketDataStatusResponse getMarketDataProviderStatus() {
        Instant checkedAt = Instant.now();
        try {
            var quote = marketDataService.getQuote("AAPL");
            return new AdminMarketDataStatusResponse(
                    provider,
                    "UP",
                    quote.symbol(),
                    quote.currentPrice(),
                    checkedAt,
                    "Provider quote check succeeded");
        } catch (RuntimeException exception) {
            return new AdminMarketDataStatusResponse(
                    provider,
                    "DOWN",
                    "AAPL",
                    null,
                    checkedAt,
                    "Provider quote check failed");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private AuditCategory parseCategory(String category) {
        try {
            return AuditCategory.valueOf(category.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuditCategoryException();
        }
    }
}
