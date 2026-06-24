package com.investrocket.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.exception.InsufficientFundsException;
import com.investrocket.exception.InsufficientHoldingsException;
import com.investrocket.exception.InvalidOrderRequestException;
import com.investrocket.exception.OrderNotFoundException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.order.dto.CreateOrderRequest;
import com.investrocket.portfolio.Holding;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.trade.Trade;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;
import com.investrocket.user.RiskSettingsService;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private RiskSettingsService riskSettingsService;

    private OrderService orderService;
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                tradeRepository,
                holdingRepository,
                walletRepository,
                marketDataService);
        orderService.setRiskSettingsService(riskSettingsService);
        user = new User("Demo User", "demo@example.com", "hashed-password");
        wallet = new Wallet(user);
    }

    @Test
    void validatesEstimatedOrderValueAgainstRiskSettings() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "195.25"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request =
                request("AAPL", OrderSide.BUY, OrderType.MARKET, 2, null, null);
        orderService.placeOrder(request, user);

        verify(riskSettingsService).validateOrderAgainstRiskControls(
                request,
                user,
                new BigDecimal("390.50"));
    }

    @Test
    void executesMarketBuyAndCreatesHoldingAndTrade() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "195.25"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.placeOrder(
                request("aapl", OrderSide.BUY, OrderType.MARKET, 2, null, null),
                user);

        assertThat(response.symbol()).isEqualTo("AAPL");
        assertThat(response.totalAmount()).isEqualByComparingTo("390.50");
        assertThat(wallet.getCashBalance()).isEqualByComparingTo("99609.50");

        ArgumentCaptor<Holding> holdingCaptor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(holdingCaptor.capture());
        assertThat(holdingCaptor.getValue().getQuantity()).isEqualTo(2);
        assertThat(holdingCaptor.getValue().getAverageBuyPrice()).isEqualByComparingTo("195.25");

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).save(tradeCaptor.capture());
        assertThat(tradeCaptor.getValue().getRealizedProfitLoss()).isEqualByComparingTo("0.00");
    }

    @Test
    void executesMarketSellAndCalculatesRealizedProfitLoss() {
        Holding holding = new Holding(user, "AAPL", "Apple Inc.", 2, new BigDecimal("100.00"));
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "120.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.of(holding));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.placeOrder(
                request("AAPL", OrderSide.SELL, OrderType.MARKET, 1, null, null),
                user);

        assertThat(wallet.getCashBalance()).isEqualByComparingTo("100120.00");
        assertThat(holding.getQuantity()).isEqualTo(1);

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).save(tradeCaptor.capture());
        assertThat(tradeCaptor.getValue().getRealizedProfitLoss()).isEqualByComparingTo("20.00");
        verify(holdingRepository, never()).delete(holding);
    }

    @Test
    void deletesHoldingWhenFinalShareIsSold() {
        Holding holding = new Holding(user, "AAPL", "Apple Inc.", 1, new BigDecimal("100.00"));
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "120.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.of(holding));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.placeOrder(
                request("AAPL", OrderSide.SELL, OrderType.MARKET, 1, null, null),
                user);

        verify(holdingRepository).delete(holding);
    }

    @Test
    void rejectsBuyWhenCashIsInsufficient() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "60000.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> orderService.placeOrder(
                request("AAPL", OrderSide.BUY, OrderType.MARKET, 2, null, null),
                user))
                .isInstanceOf(InsufficientFundsException.class);

        verify(orderRepository, never()).save(any());
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void rejectsSellWhenQuantityExceedsHolding() {
        Holding holding = new Holding(user, "AAPL", "Apple Inc.", 1, new BigDecimal("100.00"));
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "120.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.of(holding));

        assertThatThrownBy(() -> orderService.placeOrder(
                request("AAPL", OrderSide.SELL, OrderType.MARKET, 2, null, null),
                user))
                .isInstanceOf(InsufficientHoldingsException.class);
    }

    @Test
    void rejectsStopLossBuyBeforeFetchingQuote() {
        assertThatThrownBy(() -> orderService.placeOrder(
                request("AAPL", OrderSide.BUY, OrderType.STOP_LOSS, 1, null, "100.00"),
                user))
                .isInstanceOf(InvalidOrderRequestException.class)
                .hasMessage("STOP_LOSS orders are supported for SELL only in Phase 4");

        verify(marketDataService, never()).getQuote(any());
    }

    @Test
    void createsPendingLimitBuyAndReservesCash() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "195.25"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.placeOrder(
                request("AAPL", OrderSide.BUY, OrderType.LIMIT, 2, "180.00", null),
                user);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo("360.00");
        assertThat(wallet.getCashBalance()).isEqualByComparingTo("99640.00");
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo("360.00");
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void createsPendingLimitSellAndLocksShares() {
        Holding holding = new Holding(user, "AAPL", "Apple Inc.", 3, new BigDecimal("100.00"));
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "120.00"));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.of(holding));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.placeOrder(
                request("AAPL", OrderSide.SELL, OrderType.LIMIT, 2, "200.00", null),
                user);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(holding.getLockedQuantity()).isEqualTo(2);
        assertThat(holding.getAvailableQuantity()).isEqualTo(1);
        verify(tradeRepository, never()).save(any());
    }

    @Test
    void cancellationReleasesReservedCash() {
        Order order = Order.pending(
                user,
                "AAPL",
                OrderSide.BUY,
                OrderType.LIMIT,
                2,
                new BigDecimal("195.25"),
                new BigDecimal("180.00"),
                null,
                new BigDecimal("360.00"));
        wallet.reserve(new BigDecimal("360.00"));
        when(orderRepository.findForUpdateById(order.getId())).thenReturn(Optional.of(order));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));

        var response = orderService.cancelOrder(order.getId(), user);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo("0.00");
        assertThat(wallet.getCashBalance()).isEqualByComparingTo("100000.00");
    }

    @Test
    void cancellationUnlocksReservedShares() {
        Holding holding = new Holding(user, "AAPL", "Apple Inc.", 3, new BigDecimal("100.00"));
        holding.lock(2);
        Order order = Order.pending(
                user,
                "AAPL",
                OrderSide.SELL,
                OrderType.LIMIT,
                2,
                new BigDecimal("120.00"),
                new BigDecimal("200.00"),
                null,
                new BigDecimal("400.00"));
        when(orderRepository.findForUpdateById(order.getId())).thenReturn(Optional.of(order));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.of(holding));

        orderService.cancelOrder(order.getId(), user);

        assertThat(holding.getLockedQuantity()).isZero();
        assertThat(holding.getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void cannotCancelAnotherUsersOrder() {
        User anotherUser = new User("Other User", "other@example.com", "hashed-password");
        Order order = Order.pending(
                anotherUser,
                "AAPL",
                OrderSide.BUY,
                OrderType.LIMIT,
                1,
                new BigDecimal("195.25"),
                new BigDecimal("180.00"),
                null,
                new BigDecimal("180.00"));
        when(orderRepository.findForUpdateById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(order.getId(), user))
                .isInstanceOf(OrderNotFoundException.class);

        verify(walletRepository, never()).findForUpdateByUser(any());
    }

    @Test
    void rejectsLimitOrderWithoutPositiveLimitPrice() {
        assertThatThrownBy(() -> orderService.placeOrder(
                request("AAPL", OrderSide.BUY, OrderType.LIMIT, 1, null, null),
                user))
                .isInstanceOf(InvalidOrderRequestException.class)
                .hasMessage("LIMIT orders require limitPrice greater than 0");

        verify(marketDataService, never()).getQuote(any());
    }

    @Test
    void executesTriggeredPendingBuyOnceAndSettlesReservation() {
        Order order = Order.pending(
                user,
                "AAPL",
                OrderSide.BUY,
                OrderType.LIMIT,
                2,
                new BigDecimal("195.25"),
                new BigDecimal("180.00"),
                null,
                new BigDecimal("360.00"));
        wallet.reserve(new BigDecimal("360.00"));
        when(orderRepository.findForUpdateById(order.getId())).thenReturn(Optional.of(order));
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "175.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());

        var firstExecution = orderService.executePendingOrder(order.getId());
        var secondExecution = orderService.executePendingOrder(order.getId());

        assertThat(firstExecution).isPresent();
        assertThat(secondExecution).isEmpty();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo("0.00");
        assertThat(wallet.getCashBalance()).isEqualByComparingTo("99650.00");
        verify(tradeRepository, times(1)).save(any());
    }

    @Test
    void executesLimitOrderImmediatelyWhenTriggerAlreadyMatches() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "175.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.placeOrder(
                request("AAPL", OrderSide.BUY, OrderType.LIMIT, 1, "180.00", null),
                user);

        assertThat(response.status()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(response.executedPrice()).isEqualByComparingTo("175.0000");
        assertThat(wallet.getReservedBalance()).isEqualByComparingTo("0.00");
    }

    private CreateOrderRequest request(
            String symbol,
            OrderSide side,
            OrderType type,
            int quantity,
            String limitPrice,
            String stopPrice) {
        return new CreateOrderRequest(
                symbol,
                side,
                type,
                quantity,
                limitPrice == null ? null : new BigDecimal(limitPrice),
                stopPrice == null ? null : new BigDecimal(stopPrice));
    }

    private StockQuoteResponse quote(String symbol, String price) {
        BigDecimal currentPrice = new BigDecimal(price);
        return new StockQuoteResponse(
                symbol,
                "Apple Inc.",
                currentPrice,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                currentPrice,
                currentPrice,
                currentPrice,
                currentPrice,
                1L,
                Instant.now(),
                "USD",
                "mock");
    }
}
