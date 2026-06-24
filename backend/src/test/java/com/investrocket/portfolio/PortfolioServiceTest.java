package com.investrocket.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.user.User;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private MarketDataService marketDataService;

    @Test
    void calculatesHoldingsAndPortfolioSummary() {
        User user = new User("Demo User", "demo@example.com", "hashed-password");
        Wallet wallet = new Wallet(user);
        wallet.debit(new BigDecimal("200.00"));
        wallet.reserve(new BigDecimal("300.00"));
        Holding holding = new Holding(user, "AAPL", "Apple Inc.", 2, new BigDecimal("100.00"));
        holding.lock(1);

        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findByUser(user)).thenReturn(List.of(holding));
        when(marketDataService.getQuote("AAPL")).thenReturn(new StockQuoteResponse(
                "AAPL", "Apple Inc.", new BigDecimal("120.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 1L, Instant.now(), "USD", "mock"));

        PortfolioService service = new PortfolioService(
                holdingRepository,
                walletRepository,
                marketDataService);

        var holdings = service.getHoldings(user);
        var summary = service.getPortfolioSummary(user);

        assertThat(holdings).hasSize(1);
        assertThat(holdings.getFirst().currentValue()).isEqualByComparingTo("240.00");
        assertThat(holdings.getFirst().unrealizedProfitLoss()).isEqualByComparingTo("40.00");
        assertThat(holdings.getFirst().unrealizedProfitLossPercent()).isEqualByComparingTo("20.00");
        assertThat(holdings.getFirst().lockedQuantity()).isEqualTo(1);
        assertThat(holdings.getFirst().availableQuantity()).isEqualTo(1);
        assertThat(summary.availableCash()).isEqualByComparingTo("99500.00");
        assertThat(summary.reservedCash()).isEqualByComparingTo("300.00");
        assertThat(summary.totalCash()).isEqualByComparingTo("99800.00");
        assertThat(summary.holdingsValue()).isEqualByComparingTo("240.00");
        assertThat(summary.totalPortfolioValue()).isEqualByComparingTo("100040.00");
        assertThat(summary.numberOfHoldings()).isEqualTo(1);
    }
}
