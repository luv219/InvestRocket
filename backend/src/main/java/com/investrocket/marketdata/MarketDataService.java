package com.investrocket.marketdata;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.investrocket.exception.InvalidMarketDataRequestException;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;
import com.investrocket.websocket.LivePriceService;

@Service
public class MarketDataService {

    private static final Pattern VALID_SYMBOL = Pattern.compile("[A-Z0-9.-]{1,15}");

    private final MarketDataProvider marketDataProvider;
    private final LivePriceService livePriceService;

    public MarketDataService(
            MarketDataProvider marketDataProvider,
            LivePriceService livePriceService) {
        this.marketDataProvider = marketDataProvider;
        this.livePriceService = livePriceService;
    }

    public List<StockSearchResult> searchStocks(String query) {
        if (query == null || query.isBlank()) {
            throw new InvalidMarketDataRequestException("Search query is required");
        }
        return marketDataProvider.searchStocks(query.trim());
    }

    public StockQuoteResponse getQuote(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidMarketDataRequestException("Stock symbol is required");
        }
        String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);
        if (!VALID_SYMBOL.matcher(normalizedSymbol).matches()) {
            throw new InvalidMarketDataRequestException("Stock symbol is invalid");
        }
        return livePriceService.applyLatestPrice(
                marketDataProvider.getQuote(normalizedSymbol));
    }
}
