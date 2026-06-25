package com.investrocket.exception;

public class ProviderRateLimitExceededException extends MarketDataProviderException {

    public ProviderRateLimitExceededException(String provider) {
        super(provider + " provider rate limit exhausted");
    }
}
