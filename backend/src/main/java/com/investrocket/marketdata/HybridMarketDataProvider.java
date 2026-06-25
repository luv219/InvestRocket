package com.investrocket.marketdata;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;

@Component
@Primary
public class HybridMarketDataProvider implements MarketDataProvider {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HybridMarketDataProvider.class);

    private final FinnhubMarketDataProvider finnhubProvider;
    private final TwelveDataMarketDataProvider twelveDataProvider;
    private final MockMarketDataProvider mockProvider;
    private final String configuredProvider;

    public HybridMarketDataProvider(
            FinnhubMarketDataProvider finnhubProvider,
            TwelveDataMarketDataProvider twelveDataProvider,
            MockMarketDataProvider mockProvider,
            @Value("${app.financial-api.provider:mock}") String configuredProvider) {
        this.finnhubProvider = finnhubProvider;
        this.twelveDataProvider = twelveDataProvider;
        this.mockProvider = mockProvider;
        this.configuredProvider = configuredProvider.trim().toLowerCase(Locale.ROOT);
        LOGGER.info("Market data provider selected: {}", this.configuredProvider);
    }

    @Override
    public List<StockSearchResult> searchStocks(String query) {
        MarketDataProvider provider = selectProvider(query);
        LOGGER.info(
                "Market data search requested: query={}, provider={}",
                query,
                providerName(provider));
        try {
            List<StockSearchResult> results = provider.searchStocks(query);
            if (results == null || results.isEmpty()) {
                return fallbackSearch(query, provider, null);
            }
            return results;
        } catch (RuntimeException exception) {
            return fallbackSearch(query, provider, exception);
        }
    }

    @Override
    public StockQuoteResponse getQuote(String symbol) {
        MarketDataProvider provider = selectProvider(symbol);
        LOGGER.info(
                "Market data quote requested: symbol={}, provider={}",
                symbol,
                providerName(provider));
        try {
            StockQuoteResponse quote = provider.getQuote(symbol);
            if (!hasValidPrice(quote)) {
                return fallbackQuote(symbol, provider, null);
            }
            return quote;
        } catch (RuntimeException exception) {
            return fallbackQuote(symbol, provider, exception);
        }
    }

    private MarketDataProvider selectProvider(String symbolOrQuery) {
        return switch (configuredProvider) {
            case "hybrid" -> isIndianSymbol(symbolOrQuery)
                    ? twelveDataProvider
                    : finnhubProvider;
            case "finnhub" -> finnhubProvider;
            case "twelvedata" -> twelveDataProvider;
            case "mock" -> mockProvider;
            default -> {
                LOGGER.warn(
                        "Unsupported market data provider '{}'; using mock",
                        configuredProvider);
                yield mockProvider;
            }
        };
    }

    private List<StockSearchResult> fallbackSearch(
            String query,
            MarketDataProvider failedProvider,
            RuntimeException exception) {
        if (failedProvider == mockProvider) {
            return List.of();
        }
        logFallback(query, failedProvider, exception);
        return mockProvider.searchStocks(query);
    }

    private StockQuoteResponse fallbackQuote(
            String symbol,
            MarketDataProvider failedProvider,
            RuntimeException exception) {
        if (failedProvider != mockProvider) {
            logFallback(symbol, failedProvider, exception);
        }
        return mockProvider.getQuote(symbol);
    }

    private void logFallback(
            String symbolOrQuery,
            MarketDataProvider failedProvider,
            RuntimeException exception) {
        String reason = exception == null
                ? "empty or invalid response"
                : exception.getClass().getSimpleName();
        LOGGER.warn(
                "Market data fallback used: request={}, provider={}, reason={}",
                symbolOrQuery,
                providerName(failedProvider),
                reason);
    }

    private boolean hasValidPrice(StockQuoteResponse quote) {
        BigDecimal currentPrice = quote == null ? null : quote.currentPrice();
        return currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isIndianSymbol(String symbolOrQuery) {
        String normalized = symbolOrQuery.trim().toUpperCase(Locale.ROOT);
        return normalized.endsWith(".NS") || normalized.endsWith(".BO");
    }

    private String providerName(MarketDataProvider provider) {
        if (provider == finnhubProvider) {
            return "finnhub";
        }
        if (provider == twelveDataProvider) {
            return "twelvedata";
        }
        return "mock";
    }
}
