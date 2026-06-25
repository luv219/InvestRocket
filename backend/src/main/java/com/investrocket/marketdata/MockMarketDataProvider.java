package com.investrocket.marketdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;

@Component
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
            return generatedQuote(symbol);
        }
        return stock.toQuoteResponse();
    }

    private StockQuoteResponse generatedQuote(String symbol) {
        String normalizedSymbol = symbol.toUpperCase(Locale.ROOT);
        long seed = Integer.toUnsignedLong(normalizedSymbol.hashCode());
        BigDecimal currentPrice = BigDecimal.valueOf(25 + seed % 47_500)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal previousClose = currentPrice.multiply(new BigDecimal("0.9975"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal changeAmount = currentPrice.subtract(previousClose)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal changePercent = changeAmount
                .divide(previousClose, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        boolean indianSymbol = normalizedSymbol.endsWith(".NS")
                || normalizedSymbol.endsWith(".BO");

        return new StockQuoteResponse(
                normalizedSymbol,
                normalizedSymbol + " Simulated Quote",
                currentPrice,
                changeAmount,
                changePercent,
                previousClose,
                currentPrice.multiply(new BigDecimal("1.01"))
                        .setScale(2, RoundingMode.HALF_UP),
                previousClose.multiply(new BigDecimal("0.99"))
                        .setScale(2, RoundingMode.HALF_UP),
                previousClose,
                100_000L + seed % 9_900_000L,
                Instant.now(),
                indianSymbol ? "INR" : "USD",
                "mock");
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
        stocks.put("RELIANCE.NS", indianStock(
                "RELIANCE.NS", "Reliance Industries Limited", "2965.40", "18.30"));
        stocks.put("TCS.NS", indianStock(
                "TCS.NS", "Tata Consultancy Services Limited", "3812.65", "-12.45"));
        stocks.put("INFY.NS", indianStock(
                "INFY.NS", "Infosys Limited", "1598.20", "9.75"));
        stocks.put("HDFCBANK.NS", indianStock(
                "HDFCBANK.NS", "HDFC Bank Limited", "1734.55", "6.80"));
        stocks.put("ICICIBANK.NS", indianStock(
                "ICICIBANK.NS", "ICICI Bank Limited", "1421.35", "-4.25"));
        stocks.put("SBIN.NS", indianStock(
                "SBIN.NS", "State Bank of India", "812.70", "3.60"));
        stocks.put("ITC.NS", indianStock(
                "ITC.NS", "ITC Limited", "432.15", "1.85"));
        stocks.put("LT.NS", indianStock(
                "LT.NS", "Larsen & Toubro Limited", "3650.80", "22.10"));
        stocks.put("BHARTIARTL.NS", indianStock(
                "BHARTIARTL.NS", "Bharti Airtel Limited", "1872.40", "11.25"));
        stocks.put("HINDUNILVR.NS", indianStock(
                "HINDUNILVR.NS", "Hindustan Unilever Limited", "2418.90", "-8.10"));
        return Map.copyOf(stocks);
    }

    private static MockStock indianStock(
            String symbol,
            String companyName,
            String currentPrice,
            String changeAmount) {
        BigDecimal current = new BigDecimal(currentPrice);
        BigDecimal change = new BigDecimal(changeAmount);
        BigDecimal previousClose = current.subtract(change);
        BigDecimal changePercent = change
                .divide(previousClose, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return new MockStock(
                symbol,
                companyName,
                "NSE",
                "INR",
                "Common Stock",
                current.toPlainString(),
                change.toPlainString(),
                changePercent.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                previousClose.multiply(new BigDecimal("1.001")).setScale(2, RoundingMode.HALF_UP)
                        .toPlainString(),
                current.multiply(new BigDecimal("1.008")).setScale(2, RoundingMode.HALF_UP)
                        .toPlainString(),
                previousClose.multiply(new BigDecimal("0.992")).setScale(2, RoundingMode.HALF_UP)
                        .toPlainString(),
                previousClose.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                5_000_000L);
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
