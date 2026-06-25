package com.investrocket.marketdata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.investrocket.exception.MarketDataConfigurationException;
import com.investrocket.ratelimit.ProviderRateLimiter;

class FinnhubMarketDataProviderTest {

    @Test
    void requiresApiKeyBeforeMakingRequests() {
        FinnhubMarketDataProvider provider = new FinnhubMarketDataProvider(
                RestClient.builder(),
                "https://finnhub.io/api/v1",
                "",
                mock(ProviderRateLimiter.class));

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(MarketDataConfigurationException.class)
                .hasMessageContaining("FINNHUB_API_KEY");
    }
}
