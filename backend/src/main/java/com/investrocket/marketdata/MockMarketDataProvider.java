package com.investrocket.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;
import com.investrocket.exception.StockNotFoundException;

@Component
@ConditionalOnProperty(
        prefix = "app.financial-api",
        name = "provider",
        havingValue = "mock",
        matchIfMissing = true)
public class MockMarketDataProvider implements MarketDataProvider {

    private static final Map<String, MockStock> STOCKS = createStocks();

    @Override
    public List<StockSearchResult> searchStocks(String query) {
        String normalizedQuery = query.toUpperCase(Locale.ROOT);
        return STOCKS.values().stream()
                .filter(stock -> stock.symbol().contains(normalizedQuery)
                        || stock.companyName().toUpperCase(Locale.ROOT).contains(normalizedQuery))
                .map(MockStock::toSearchResult)
                .toList();
    }

    @Override
    public StockQuoteResponse getQuote(String symbol) {
        MockStock stock = STOCKS.get(symbol);
        if (stock == null) {
            throw new StockNotFoundException(symbol);
        }
        return stock.toQuoteResponse();
    }

    private static Map<String, MockStock> createStocks() {
        Map<String, MockStock> stocks = new LinkedHashMap<>();
        stocks.put("AAPL", new MockStock(
                "AAPL", "Apple Inc.", "NASDAQ", "USD", "Common Stock",
                "195.25", "1.45", "0.75", "193.00", "196.10", "192.70",
                "193.80", 58_400_000L));
        stocks.put("MSFT", new MockStock(
                "MSFT", "Microsoft Corporation", "NASDAQ", "USD", "Common Stock",
                "449.78", "2.34", "0.52", "447.10", "451.20", "446.55",
                "447.44", 21_850_000L));
        stocks.put("TSLA", new MockStock(
                "TSLA", "Tesla, Inc.", "NASDAQ", "USD", "Common Stock",
                "332.05", "-4.21", "-1.25", "337.40", "339.10", "329.82",
                "336.26", 102_300_000L));
        stocks.put("AMZN", new MockStock(
                "AMZN", "Amazon.com, Inc.", "NASDAQ", "USD", "Common Stock",
                "226.63", "1.18", "0.52", "225.10", "228.05", "224.75",
                "225.45", 42_600_000L));
        stocks.put("GOOGL", new MockStock(
                "GOOGL", "Alphabet Inc.", "NASDAQ", "USD", "Common Stock",
                "176.35", "-0.62", "-0.35", "177.20", "178.10", "175.80",
                "176.97", 29_400_000L));
        stocks.put("NVDA", new MockStock(
                "NVDA", "NVIDIA Corporation", "NASDAQ", "USD", "Common Stock",
                "147.90", "3.15", "2.18", "145.20", "149.35", "144.88",
                "144.75", 198_500_000L));
        stocks.put("META", new MockStock(
                "META", "Meta Platforms, Inc.", "NASDAQ", "USD", "Common Stock",
                "610.42", "5.67", "0.94", "604.80", "613.25", "603.90",
                "604.75", 18_750_000L));
        return Map.copyOf(stocks);
    }

    private record MockStock(
            String symbol,
            String companyName,
            String exchange,
            String currency,
            String type,
            String currentPrice,
            String changeAmount,
            String changePercent,
            String openPrice,
            String highPrice,
            String lowPrice,
            String previousClose,
            Long volume) {

        StockSearchResult toSearchResult() {
            return new StockSearchResult(symbol, companyName, exchange, currency, type);
        }

        StockQuoteResponse toQuoteResponse() {
            return new StockQuoteResponse(
                    symbol,
                    companyName,
                    decimal(currentPrice),
                    decimal(changeAmount),
                    decimal(changePercent),
                    decimal(openPrice),
                    decimal(highPrice),
                    decimal(lowPrice),
                    decimal(previousClose),
                    volume,
                    Instant.now(),
                    currency,
                    "mock");
        }

        private BigDecimal decimal(String value) {
            return new BigDecimal(value);
        }
    }
}
