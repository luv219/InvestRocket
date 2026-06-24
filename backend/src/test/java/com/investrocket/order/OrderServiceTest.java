package com.investrocket.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.investrocket.exception.UnsupportedOrderTypeException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.order.dto.CreateOrderRequest;
import com.investrocket.portfolio.Holding;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.trade.Trade;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;
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
        user = new User("Demo User", "demo@example.com", "hashed-password");
        wallet = new Wallet(user);
    }

    @Test
    void executesMarketBuyAndCreatesHoldingAndTrade() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "195.25"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findForUpdateByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.placeOrder(
                new CreateOrderRequest("aapl", OrderSide.BUY, OrderType.MARKET, 2),
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
                new CreateOrderRequest("AAPL", OrderSide.SELL, OrderType.MARKET, 1),
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
                new CreateOrderRequest("AAPL", OrderSide.SELL, OrderType.MARKET, 1),
                user);

        verify(holdingRepository).delete(holding);
    }

    @Test
    void rejectsBuyWhenCashIsInsufficient() {
        when(marketDataService.getQuote("AAPL")).thenReturn(quote("AAPL", "60000.00"));
        when(walletRepository.findForUpdateByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> orderService.placeOrder(
                new CreateOrderRequest("AAPL", OrderSide.BUY, OrderType.MARKET, 2),
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
                new CreateOrderRequest("AAPL", OrderSide.SELL, OrderType.MARKET, 2),
                user))
                .isInstanceOf(InsufficientHoldingsException.class);
    }

    @Test
    void rejectsNonMarketOrderBeforeFetchingQuote() {
        assertThatThrownBy(() -> orderService.placeOrder(
                new CreateOrderRequest("AAPL", OrderSide.BUY, OrderType.LIMIT, 1),
                user))
                .isInstanceOf(UnsupportedOrderTypeException.class)
                .hasMessage("Only MARKET orders are supported in Phase 3.");

        verify(marketDataService, never()).getQuote(any());
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
