package com.investrocket.ratelimit;

import com.investrocket.exception.ProviderRateLimitExceededException;

import io.github.bucket4j.Bucket;

public class ProviderRateLimiter {

    private final RateLimitProperties properties;
    private final Bucket finnhubBucket;
    private final Bucket twelveDataBucket;

    public ProviderRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
        this.finnhubBucket =
                RateLimitBucketFactory.perMinute(properties.getFinnhubPerMinute());
        this.twelveDataBucket =
                RateLimitBucketFactory.perMinute(properties.getTwelveDataPerMinute());
    }

    public boolean tryAcquire(Provider provider) {
        if (!properties.isEnabled()) {
            return true;
        }
        return switch (provider) {
            case FINNHUB -> finnhubBucket.tryConsume(1);
            case TWELVE_DATA -> twelveDataBucket.tryConsume(1);
        };
    }

    public void acquireOrThrow(Provider provider) {
        if (!tryAcquire(provider)) {
            throw new ProviderRateLimitExceededException(provider.displayName);
        }
    }

    public enum Provider {
        FINNHUB("Finnhub"),
        TWELVE_DATA("Twelve Data");

        private final String displayName;

        Provider(String displayName) {
            this.displayName = displayName;
        }
    }
}
