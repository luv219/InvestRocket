package com.investrocket.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.websocket.dto.LivePriceUpdate;

@Service
public class LivePriceService {

    private final Map<String, LivePriceUpdate> latestPrices = new ConcurrentHashMap<>();

    public void update(LivePriceUpdate update) {
        latestPrices.put(update.symbol(), update);
    }

    public LivePriceUpdate getLatest(String symbol) {
        return latestPrices.get(symbol);
    }

    public StockQuoteResponse applyLatestPrice(StockQuoteResponse quote) {
        LivePriceUpdate latest = getLatest(quote.symbol());
        if (latest == null) {
            return quote;
        }
        return new StockQuoteResponse(
                quote.symbol(),
                quote.companyName(),
                latest.currentPrice(),
                latest.changeAmount(),
                latest.changePercent(),
                quote.openPrice(),
                quote.highPrice(),
                quote.lowPrice(),
                quote.previousClose(),
                quote.volume(),
                latest.latestTradingTime(),
                quote.currency(),
                latest.provider());
    }
}
