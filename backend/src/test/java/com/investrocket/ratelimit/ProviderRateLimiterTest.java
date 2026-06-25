package com.investrocket.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.investrocket.exception.ProviderRateLimitExceededException;
import com.investrocket.ratelimit.ProviderRateLimiter.Provider;

class ProviderRateLimiterTest {

    @Test
    void rejectsProviderRequestAfterGlobalLimitIsExhausted() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setFinnhubPerMinute(1);
        ProviderRateLimiter rateLimiter = new ProviderRateLimiter(properties);

        assertThat(rateLimiter.tryAcquire(Provider.FINNHUB)).isTrue();
        assertThatThrownBy(() -> rateLimiter.acquireOrThrow(Provider.FINNHUB))
                .isInstanceOf(ProviderRateLimitExceededException.class);
    }
}
