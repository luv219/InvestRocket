package com.investrocket.exception;

public class MarketDataRateLimitException extends MarketDataProviderException {

    public MarketDataRateLimitException() {
        super("Financial API rate limit exceeded");
    }
}
