package com.investrocket.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.investrocket.exception.StockNotFoundException;

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
    void rejectsUnknownSymbol() {
        assertThatThrownBy(() -> provider.getQuote("UNKNOWN"))
                .isInstanceOf(StockNotFoundException.class);
    }
}
