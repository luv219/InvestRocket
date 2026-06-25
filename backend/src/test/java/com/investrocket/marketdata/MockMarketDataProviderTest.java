package com.investrocket.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MockMarketDataProviderTest {

    private final MockMarketDataProvider provider = new MockMarketDataProvider();

    @Test
    void searchesBySymbolAndCompanyName() {
        assertThat(provider.searchStocks("AAPL"))
                .extracting(result -> result.symbol())
                .containsExactly("AAPL");
        assertThat(provider.searchStocks("Microsoft"))
                .extracting(result -> result.symbol())
                .containsExactly("MSFT");
    }

    @Test
    void returnsRealisticMockQuote() {
        var quote = provider.getQuote("AAPL");

        assertThat(quote.companyName()).isEqualTo("Apple Inc.");
        assertThat(quote.currentPrice()).isEqualByComparingTo("195.25");
        assertThat(quote.provider()).isEqualTo("mock");
    }

    @Test
    void returnsSafeGeneratedQuoteForUnknownSymbol() {
        var quote = provider.getQuote("UNKNOWN");

        assertThat(quote.symbol()).isEqualTo("UNKNOWN");
        assertThat(quote.currentPrice()).isPositive();
        assertThat(quote.provider()).isEqualTo("mock");
    }

    @Test
    void includesIndianSymbols() {
        var quote = provider.getQuote("RELIANCE.NS");

        assertThat(quote.currency()).isEqualTo("INR");
        assertThat(quote.currentPrice()).isPositive();
    }
}
