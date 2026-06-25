package com.investrocket.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.investrocket.exception.MarketDataProviderException;
import com.investrocket.exception.ProviderRateLimitExceededException;
import com.investrocket.marketdata.dto.StockQuoteResponse;

class HybridMarketDataProviderTest {

    private final FinnhubMarketDataProvider finnhubProvider =
            mock(FinnhubMarketDataProvider.class);
    private final TwelveDataMarketDataProvider twelveDataProvider =
            mock(TwelveDataMarketDataProvider.class);
    private final MockMarketDataProvider mockProvider = new MockMarketDataProvider();

    @Test
    void routesUsSymbolToFinnhubInHybridMode() {
        HybridMarketDataProvider provider = provider("hybrid");
        when(finnhubProvider.getQuote("AAPL")).thenReturn(quote("AAPL", "finnhub"));

        assertThat(provider.getQuote("AAPL").provider()).isEqualTo("finnhub");
        verify(finnhubProvider).getQuote("AAPL");
        verifyNoInteractions(twelveDataProvider);
    }

    @Test
    void routesIndianSymbolToTwelveDataInHybridMode() {
        HybridMarketDataProvider provider = provider("hybrid");
        when(twelveDataProvider.getQuote("RELIANCE.NS"))
                .thenReturn(quote("RELIANCE.NS", "twelvedata"));

        assertThat(provider.getQuote("RELIANCE.NS").provider()).isEqualTo("twelvedata");
        verify(twelveDataProvider).getQuote("RELIANCE.NS");
        verifyNoInteractions(finnhubProvider);
    }

    @Test
    void fallsBackToMockWhenTwelveDataFails() {
        HybridMarketDataProvider provider = provider("hybrid");
        when(twelveDataProvider.getQuote("RELIANCE.NS"))
                .thenThrow(new MarketDataProviderException("provider failed"));

        var quote = provider.getQuote("RELIANCE.NS");

        assertThat(quote.provider()).isEqualTo("mock");
        assertThat(quote.currentPrice()).isPositive();
    }

    @Test
    void fallsBackToMockWhenFinnhubFails() {
        HybridMarketDataProvider provider = provider("hybrid");
        when(finnhubProvider.getQuote("AAPL"))
                .thenThrow(new MarketDataProviderException("provider failed"));

        var quote = provider.getQuote("AAPL");

        assertThat(quote.provider()).isEqualTo("mock");
        assertThat(quote.currentPrice()).isPositive();
    }

    @Test
    void unknownSymbolReturnsSafeMockQuote() {
        HybridMarketDataProvider provider = provider("mock");

        var quote = provider.getQuote("UNKNOWN");

        assertThat(quote.symbol()).isEqualTo("UNKNOWN");
        assertThat(quote.currentPrice()).isPositive();
        assertThat(quote.provider()).isEqualTo("mock");
    }

    @Test
    void providerLimitExhaustionFallsBackToMock() {
        HybridMarketDataProvider provider = provider("hybrid");
        when(finnhubProvider.getQuote("AAPL"))
                .thenThrow(new ProviderRateLimitExceededException("Finnhub"));

        var quote = provider.getQuote("AAPL");

        assertThat(quote.provider()).isEqualTo("mock");
        verify(finnhubProvider).getQuote("AAPL");
    }

    private HybridMarketDataProvider provider(String mode) {
        return new HybridMarketDataProvider(
                finnhubProvider,
                twelveDataProvider,
                mockProvider,
                mode);
    }

    private StockQuoteResponse quote(String symbol, String provider) {
        return new StockQuoteResponse(
                symbol,
                symbol,
                new BigDecimal("100.00"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("99.00"),
                new BigDecimal("101.00"),
                new BigDecimal("98.00"),
                new BigDecimal("99.00"),
                1_000L,
                Instant.now(),
                symbol.endsWith(".NS") ? "INR" : "USD",
                provider);
    }
}
