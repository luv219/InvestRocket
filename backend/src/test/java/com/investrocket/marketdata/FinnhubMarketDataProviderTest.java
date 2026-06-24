package com.investrocket.marketdata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.investrocket.exception.MarketDataConfigurationException;

class FinnhubMarketDataProviderTest {

    @Test
    void requiresApiKeyBeforeMakingRequests() {
        FinnhubMarketDataProvider provider = new FinnhubMarketDataProvider(
                RestClient.builder(),
                "https://finnhub.io/api/v1",
                "");

        assertThatThrownBy(() -> provider.getQuote("AAPL"))
                .isInstanceOf(MarketDataConfigurationException.class)
                .hasMessageContaining("FINANCIAL_API_KEY");
    }
}
