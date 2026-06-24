package com.investrocket.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.websocket.dto.LivePriceUpdate;

@ExtendWith(MockitoExtension.class)
class PriceBroadcastServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void storesAndBroadcastsPriceToGeneralAndSymbolTopics() {
        LivePriceService livePriceService = new LivePriceService();
        PriceBroadcastService service =
                new PriceBroadcastService(messagingTemplate, livePriceService);
        StockQuoteResponse quote = quote();

        service.broadcast(quote);

        LivePriceUpdate stored = livePriceService.getLatest("AAPL");
        assertThat(stored.currentPrice()).isEqualByComparingTo("195.25");
        verify(messagingTemplate).convertAndSend("/topic/prices", stored);
        verify(messagingTemplate).convertAndSend("/topic/prices/AAPL", stored);
    }

    private StockQuoteResponse quote() {
        return new StockQuoteResponse(
                "AAPL",
                "Apple Inc.",
                new BigDecimal("195.25"),
                new BigDecimal("1.45"),
                new BigDecimal("0.75"),
                new BigDecimal("193.00"),
                new BigDecimal("196.10"),
                new BigDecimal("192.70"),
                new BigDecimal("193.80"),
                58_400_000L,
                Instant.parse("2026-06-24T16:00:00Z"),
                "USD",
                "mock-live");
    }
}
