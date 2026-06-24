package com.investrocket.marketdata;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.investrocket.auth.JwtAuthenticationFilter;
import com.investrocket.auth.JwtService;
import com.investrocket.config.CorsConfig;
import com.investrocket.config.RestAuthenticationEntryPoint;
import com.investrocket.config.SecurityConfig;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;

@WebMvcTest(MarketDataController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        RestAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
class MarketDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketDataService marketDataService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void rejectsUnauthenticatedSearch() throws Exception {
        mockMvc.perform(get("/api/market/search").param("query", "AAPL"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsAuthenticatedSearchResults() throws Exception {
        when(marketDataService.searchStocks("AAPL")).thenReturn(List.of(
                new StockSearchResult("AAPL", "Apple Inc.", "NASDAQ", "USD", "Common Stock")));

        mockMvc.perform(get("/api/market/search")
                        .param("query", "AAPL")
                        .with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].symbol").value("AAPL"));
    }

    @Test
    void rejectsMissingSearchQuery() throws Exception {
        mockMvc.perform(get("/api/market/search").with(user("demo@example.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Search query is required"));
    }

    @Test
    void returnsAuthenticatedQuote() throws Exception {
        when(marketDataService.getQuote("AAPL")).thenReturn(new StockQuoteResponse(
                "AAPL", "Apple Inc.", new BigDecimal("195.25"),
                new BigDecimal("1.45"), new BigDecimal("0.75"),
                new BigDecimal("193.00"), new BigDecimal("196.10"),
                new BigDecimal("192.70"), new BigDecimal("193.80"),
                58_400_000L, Instant.parse("2026-06-24T16:00:00Z"),
                "USD", "mock"));

        mockMvc.perform(get("/api/market/quote/AAPL").with(user("demo@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.symbol").value("AAPL"))
                .andExpect(jsonPath("$.data.currentPrice").value(195.25))
                .andExpect(jsonPath("$.data.provider").value("mock"));
    }
}
