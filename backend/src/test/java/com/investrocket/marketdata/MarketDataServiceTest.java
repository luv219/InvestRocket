package com.investrocket.marketdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.exception.InvalidMarketDataRequestException;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    @Mock
    private MarketDataProvider marketDataProvider;

    @Test
    void trimsSearchQuery() {
        MarketDataService service = new MarketDataService(marketDataProvider);
        List<StockSearchResult> expected = List.of(
                new StockSearchResult("AAPL", "Apple Inc.", "NASDAQ", "USD", "Common Stock"));
        when(marketDataProvider.searchStocks("aapl")).thenReturn(expected);

        assertThat(service.searchStocks("  aapl  ")).isEqualTo(expected);
        verify(marketDataProvider).searchStocks("aapl");
    }

    @Test
    void normalizesQuoteSymbol() {
        MarketDataService service = new MarketDataService(marketDataProvider);
        StockQuoteResponse expected = new StockQuoteResponse(
                "AAPL", "Apple Inc.", new BigDecimal("195.25"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, Instant.now(), "USD", "mock");
        when(marketDataProvider.getQuote("AAPL")).thenReturn(expected);

        assertThat(service.getQuote(" aapl ")).isEqualTo(expected);
        verify(marketDataProvider).getQuote("AAPL");
    }

    @Test
    void rejectsBlankSearchQuery() {
        MarketDataService service = new MarketDataService(marketDataProvider);

        assertThatThrownBy(() -> service.searchStocks(" "))
                .isInstanceOf(InvalidMarketDataRequestException.class)
                .hasMessage("Search query is required");
    }

    @Test
    void rejectsInvalidSymbol() {
        MarketDataService service = new MarketDataService(marketDataProvider);

        assertThatThrownBy(() -> service.getQuote("BAD SYMBOL"))
                .isInstanceOf(InvalidMarketDataRequestException.class)
                .hasMessage("Stock symbol is invalid");
    }
}
