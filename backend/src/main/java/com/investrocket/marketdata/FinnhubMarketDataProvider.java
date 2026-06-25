package com.investrocket.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.investrocket.exception.MarketDataConfigurationException;
import com.investrocket.exception.MarketDataProviderException;
import com.investrocket.exception.MarketDataRateLimitException;
import com.investrocket.exception.StockNotFoundException;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;
import com.investrocket.ratelimit.ProviderRateLimiter;
import com.investrocket.ratelimit.ProviderRateLimiter.Provider;

@Component
public class FinnhubMarketDataProvider implements MarketDataProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final ProviderRateLimiter providerRateLimiter;

    public FinnhubMarketDataProvider(
            RestClient.Builder restClientBuilder,
            @Value("${app.financial-api.finnhub-base-url}") String baseUrl,
            @Value("${app.financial-api.finnhub-api-key:}") String apiKey,
            ProviderRateLimiter providerRateLimiter) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.providerRateLimiter = providerRateLimiter;
    }

    @Override
    public List<StockSearchResult> searchStocks(String query) {
        requireApiKey();
        try {
            providerRateLimiter.acquireOrThrow(Provider.FINNHUB);
            FinnhubSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(FinnhubSearchResponse.class);

            if (response == null || response.results() == null) {
                return List.of();
            }

            return response.results().stream()
                    .filter(result -> result.symbol() != null && !result.symbol().isBlank())
                    .map(result -> new StockSearchResult(
                            result.symbol(),
                            firstNonBlank(result.description(), result.displaySymbol(), result.symbol()),
                            result.exchange(),
                            null,
                            result.type()))
                    .toList();
        } catch (RestClientResponseException exception) {
            throw translateProviderException(exception);
        } catch (MarketDataProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MarketDataProviderException("Unable to search stocks with Finnhub", exception);
        }
    }

    @Override
    public StockQuoteResponse getQuote(String symbol) {
        requireApiKey();
        try {
            providerRateLimiter.acquireOrThrow(Provider.FINNHUB);
            FinnhubQuoteResponse quote = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote")
                            .queryParam("symbol", symbol)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(FinnhubQuoteResponse.class);
            providerRateLimiter.acquireOrThrow(Provider.FINNHUB);
            FinnhubProfileResponse profile = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stock/profile2")
                            .queryParam("symbol", symbol)
                            .queryParam("token", apiKey)
                            .build())
                    .retrieve()
                    .body(FinnhubProfileResponse.class);

            if (quote == null || quote.currentPrice() == null
                    || quote.currentPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new StockNotFoundException(symbol);
            }

            return new StockQuoteResponse(
                    symbol,
                    profile == null ? symbol : firstNonBlank(profile.name(), symbol),
                    quote.currentPrice(),
                    quote.changeAmount(),
                    quote.changePercent(),
                    quote.openPrice(),
                    quote.highPrice(),
                    quote.lowPrice(),
                    quote.previousClose(),
                    null,
                    quote.timestamp() == null
                            ? Instant.now()
                            : Instant.ofEpochSecond(quote.timestamp()),
                    profile == null ? null : profile.currency(),
                    "finnhub");
        } catch (StockNotFoundException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw translateProviderException(exception);
        } catch (MarketDataProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MarketDataProviderException("Unable to fetch quote from Finnhub", exception);
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new MarketDataConfigurationException(
                    "FINNHUB_API_KEY or FINANCIAL_API_KEY is required when using Finnhub");
        }
    }

    private MarketDataProviderException translateProviderException(
            RestClientResponseException exception) {
        if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return new MarketDataRateLimitException();
        }
        if (exception.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()
                || exception.getStatusCode().value() == HttpStatus.FORBIDDEN.value()) {
            return new MarketDataConfigurationException(
                    "Finnhub rejected the configured financial API key");
        }
        return new MarketDataProviderException("Finnhub market data request failed", exception);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FinnhubSearchResponse(
            @JsonProperty("result") List<FinnhubSearchItem> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FinnhubSearchItem(
            String description,
            String displaySymbol,
            String symbol,
            String type,
            String exchange) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FinnhubQuoteResponse(
            @JsonProperty("c") BigDecimal currentPrice,
            @JsonProperty("d") BigDecimal changeAmount,
            @JsonProperty("dp") BigDecimal changePercent,
            @JsonProperty("o") BigDecimal openPrice,
            @JsonProperty("h") BigDecimal highPrice,
            @JsonProperty("l") BigDecimal lowPrice,
            @JsonProperty("pc") BigDecimal previousClose,
            @JsonProperty("t") Long timestamp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record FinnhubProfileResponse(String name, String currency) {
    }
}
