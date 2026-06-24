package com.investrocket.websocket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.investrocket.marketdata.MarketDataProvider;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.websocket.dto.LivePriceUpdate;

@Component
@ConditionalOnExpression(
        "'${app.live-price-stream.enabled:true}' == 'true' "
                + "and '${app.financial-api.provider:mock}' == 'mock'")
public class MockLivePriceGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockLivePriceGenerator.class);
    private static final List<String> SYMBOLS =
            List.of("AAPL", "MSFT", "TSLA", "AMZN", "GOOGL", "NVDA", "META");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MarketDataProvider marketDataProvider;
    private final LivePriceService livePriceService;
    private final PriceBroadcastService priceBroadcastService;

    public MockLivePriceGenerator(
            MarketDataProvider marketDataProvider,
            LivePriceService livePriceService,
            PriceBroadcastService priceBroadcastService) {
        this.marketDataProvider = marketDataProvider;
        this.livePriceService = livePriceService;
        this.priceBroadcastService = priceBroadcastService;
    }

    @Scheduled(fixedDelayString = "${app.live-price-stream.interval-ms:5000}")
    public void generatePrices() {
        for (String symbol : SYMBOLS) {
            try {
                priceBroadcastService.broadcast(nextQuote(symbol));
            } catch (RuntimeException exception) {
                LOGGER.warn("Unable to generate demo price for {}", symbol, exception);
            }
        }
    }

    private StockQuoteResponse nextQuote(String symbol) {
        StockQuoteResponse baseQuote = marketDataProvider.getQuote(symbol);
        LivePriceUpdate latest = livePriceService.getLatest(symbol);
        BigDecimal currentPrice = latest == null
                ? baseQuote.currentPrice()
                : latest.currentPrice();
        BigDecimal movement = BigDecimal.valueOf(
                ThreadLocalRandom.current().nextDouble(-0.0025, 0.0025));
        BigDecimal nextPrice = currentPrice
                .multiply(BigDecimal.ONE.add(movement))
                .max(new BigDecimal("0.01"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal changeAmount = nextPrice
                .subtract(baseQuote.previousClose())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal changePercent = changeAmount
                .divide(baseQuote.previousClose(), 6, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);

        return new StockQuoteResponse(
                baseQuote.symbol(),
                baseQuote.companyName(),
                nextPrice,
                changeAmount,
                changePercent,
                baseQuote.openPrice(),
                baseQuote.highPrice(),
                baseQuote.lowPrice(),
                baseQuote.previousClose(),
                baseQuote.volume(),
                Instant.now(),
                baseQuote.currency(),
                "mock-live");
    }
}
