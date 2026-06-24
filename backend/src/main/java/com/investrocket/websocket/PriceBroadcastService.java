package com.investrocket.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.websocket.dto.LivePriceUpdate;

@Service
public class PriceBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final LivePriceService livePriceService;

    public PriceBroadcastService(
            SimpMessagingTemplate messagingTemplate,
            LivePriceService livePriceService) {
        this.messagingTemplate = messagingTemplate;
        this.livePriceService = livePriceService;
    }

    public void broadcast(StockQuoteResponse quote) {
        LivePriceUpdate update = new LivePriceUpdate(
                quote.symbol(),
                quote.currentPrice(),
                quote.changeAmount(),
                quote.changePercent(),
                quote.latestTradingTime(),
                quote.provider());
        livePriceService.update(update);
        messagingTemplate.convertAndSend("/topic/prices", update);
        messagingTemplate.convertAndSend("/topic/prices/" + update.symbol(), update);
    }
}
