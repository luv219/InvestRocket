package com.investrocket.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

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
public class TwelveDataMarketDataProvider implements MarketDataProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final ProviderRateLimiter providerRateLimiter;

    public TwelveDataMarketDataProvider(
            RestClient.Builder restClientBuilder,
            @Value("${app.financial-api.twelve-data-base-url}") String baseUrl,
            @Value("${app.financial-api.twelve-data-api-key:}") String apiKey,
            ProviderRateLimiter providerRateLimiter) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.providerRateLimiter = providerRateLimiter;
    }

    @Override
    public List<StockSearchResult> searchStocks(String query) {
        requireApiKey();
        TwelveDataSymbol requested = TwelveDataSymbol.from(query);
        try {
            providerRateLimiter.acquireOrThrow(Provider.TWELVE_DATA);
            TwelveDataSearchResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/symbol_search")
                                .queryParam("symbol", requested.baseSymbol())
                                .queryParam("apikey", apiKey);
                        if (requested.exchange() != null) {
                            builder.queryParam("exchange", requested.exchange());
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .body(TwelveDataSearchResponse.class);

            if (response == null || response.data() == null) {
                return List.of();
            }
            return response.data().stream()
                    .filter(item -> item.symbol() != null && !item.symbol().isBlank())
                    .map(item -> new StockSearchResult(
                            requested.externalSymbol(item.symbol()),
                            firstNonBlank(item.instrumentName(), item.symbol()),
                            firstNonBlank(item.exchange(), requested.displayExchange()),
                            item.currency(),
                            item.instrumentType()))
                    .toList();
        } catch (RestClientResponseException exception) {
            throw translateProviderException(exception);
        } catch (MarketDataProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MarketDataProviderException(
                    "Unable to search stocks with Twelve Data", exception);
        }
    }

    @Override
    public StockQuoteResponse getQuote(String symbol) {
        requireApiKey();
        TwelveDataSymbol requested = TwelveDataSymbol.from(symbol);
        try {
            providerRateLimiter.acquireOrThrow(Provider.TWELVE_DATA);
            TwelveDataQuoteResponse quote = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/quote")
                                .queryParam("symbol", requested.baseSymbol())
                                .queryParam("apikey", apiKey);
                        if (requested.exchange() != null) {
                            builder.queryParam("exchange", requested.exchange());
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .body(TwelveDataQuoteResponse.class);

            if (quote == null || "error".equalsIgnoreCase(quote.status())
                    || quote.close() == null
                    || quote.close().compareTo(BigDecimal.ZERO) <= 0) {
                throw new StockNotFoundException(symbol);
            }

            return new StockQuoteResponse(
                    symbol,
                    firstNonBlank(quote.name(), symbol),
                    quote.close(),
                    quote.change(),
                    quote.percentChange(),
                    quote.open(),
                    quote.high(),
                    quote.low(),
                    quote.previousClose(),
                    quote.volume(),
                    quote.timestamp() == null
                            ? Instant.now()
                            : Instant.ofEpochSecond(quote.timestamp()),
                    firstNonBlank(quote.currency(), requested.defaultCurrency()),
                    "twelvedata");
        } catch (StockNotFoundException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw translateProviderException(exception);
        } catch (MarketDataProviderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new MarketDataProviderException(
                    "Unable to fetch quote from Twelve Data", exception);
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new MarketDataConfigurationException(
                    "TWELVE_DATA_API_KEY is required when using Twelve Data");
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
                    "Twelve Data rejected the configured API key");
        }
        return new MarketDataProviderException(
                "Twelve Data market data request failed", exception);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private record TwelveDataSymbol(
            String baseSymbol,
            String exchange,
            String suffix,
            String displayExchange) {

        static TwelveDataSymbol from(String symbol) {
            String normalized = symbol.trim().toUpperCase(Locale.ROOT);
            if (normalized.endsWith(".NS")) {
                return new TwelveDataSymbol(
                        normalized.substring(0, normalized.length() - 3),
                        "XNSE",
                        ".NS",
                        "NSE");
            }
            if (normalized.endsWith(".BO")) {
                return new TwelveDataSymbol(
                        normalized.substring(0, normalized.length() - 3),
                        "XBOM",
                        ".BO",
                        "BSE");
            }
            return new TwelveDataSymbol(normalized, null, "", null);
        }

        String externalSymbol(String providerSymbol) {
            String normalized = providerSymbol.toUpperCase(Locale.ROOT);
            return suffix.isBlank() || normalized.endsWith(suffix)
                    ? normalized
                    : normalized + suffix;
        }

        String defaultCurrency() {
            return suffix.isBlank() ? "USD" : "INR";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TwelveDataSearchResponse(
            List<TwelveDataSearchItem> data,
            String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TwelveDataSearchItem(
            String symbol,
            @JsonProperty("instrument_name") String instrumentName,
            String exchange,
            String currency,
            @JsonProperty("instrument_type") String instrumentType) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TwelveDataQuoteResponse(
            String symbol,
            String name,
            String exchange,
            String currency,
            Long timestamp,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            @JsonProperty("previous_close") BigDecimal previousClose,
            BigDecimal change,
            @JsonProperty("percent_change") BigDecimal percentChange,
            Long volume,
            String status) {
    }
}
