package com.investrocket.watchlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.exception.DuplicateWatchlistItemException;
import com.investrocket.exception.WatchlistItemNotFoundException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;
import com.investrocket.user.User;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private MarketDataService marketDataService;

    private WatchlistService watchlistService;
    private User user;

    @BeforeEach
    void setUp() {
        watchlistService = new WatchlistService(watchlistRepository, marketDataService);
        user = new User("Demo User", "demo@example.com", "hash");
    }

    @Test
    void addsNormalizedSymbolWithMarketMetadata() {
        StockQuoteResponse quote = quote("AAPL");
        when(watchlistRepository.existsByUserAndSymbol(user, "AAPL")).thenReturn(false);
        when(marketDataService.getQuote("AAPL")).thenReturn(quote);
        when(marketDataService.searchStocks("AAPL")).thenReturn(List.of(
                new StockSearchResult(
                        "AAPL", "Apple Inc.", "NASDAQ", "USD", "Common Stock")));
        when(watchlistRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = watchlistService.addToWatchlist(" aapl ", user);

        assertThat(response.symbol()).isEqualTo("AAPL");
        assertThat(response.exchange()).isEqualTo("NASDAQ");
        assertThat(response.currentPrice()).isEqualByComparingTo("195.25");
    }

    @Test
    void rejectsDuplicateSymbol() {
        when(watchlistRepository.existsByUserAndSymbol(user, "AAPL")).thenReturn(true);

        assertThatThrownBy(() -> watchlistService.addToWatchlist("AAPL", user))
                .isInstanceOf(DuplicateWatchlistItemException.class);
    }

    @Test
    void removesOnlyOwnedWatchlistItem() {
        WatchlistItem item =
                new WatchlistItem(user, "AAPL", "Apple Inc.", "NASDAQ", "USD");
        when(watchlistRepository.findByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.of(item));

        watchlistService.removeFromWatchlist("aapl", user);

        verify(watchlistRepository).delete(item);
    }

    @Test
    void rejectsRemovalWhenUserDoesNotOwnSymbol() {
        when(watchlistRepository.findByUserAndSymbol(user, "AAPL"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.removeFromWatchlist("AAPL", user))
                .isInstanceOf(WatchlistItemNotFoundException.class);
    }

    private StockQuoteResponse quote(String symbol) {
        return new StockQuoteResponse(
                symbol,
                "Apple Inc.",
                new BigDecimal("195.25"),
                new BigDecimal("1.45"),
                new BigDecimal("0.75"),
                new BigDecimal("193.00"),
                new BigDecimal("196.10"),
                new BigDecimal("192.70"),
                new BigDecimal("193.80"),
                58_400_000L,
                Instant.parse("2026-06-24T16:00:00Z"),
                "USD",
                "mock");
    }
}
