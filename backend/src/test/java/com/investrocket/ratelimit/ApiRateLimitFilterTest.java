package com.investrocket.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

class ApiRateLimitFilterTest {

    private final RateLimitProperties properties = propertiesWithSingleRequestLimits();
    private final ApiRateLimitFilter filter = new ApiRateLimitFilter(
            new ObjectMapper().findAndRegisterModules(),
            properties);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginEndpointReturnsTooManyRequestsAfterLimit() throws Exception {
        MockHttpServletResponse firstResponse = perform("POST", "/api/auth/login", "203.0.113.10");
        MockHttpServletResponse secondResponse = perform("POST", "/api/auth/login", "203.0.113.10");

        assertThat(firstResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(secondResponse.getStatus())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(secondResponse.getHeader("X-RateLimit-Limit")).isEqualTo("1");
        assertThat(secondResponse.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(secondResponse.getHeader("X-RateLimit-Reset")).isNotBlank();
        assertThat(secondResponse.getHeader(HttpHeaders.RETRY_AFTER)).isNotBlank();
    }

    @Test
    void marketQuoteEndpointReturnsTooManyRequestsAfterUserLimit() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "demo@example.com",
                        null,
                        List.of()));

        MockHttpServletResponse firstResponse =
                perform("GET", "/api/market/quote/AAPL", "203.0.113.11");
        MockHttpServletResponse secondResponse =
                perform("GET", "/api/market/quote/MSFT", "203.0.113.12");

        assertThat(firstResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(secondResponse.getStatus())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void healthEndpointIsNotRateLimited() throws Exception {
        for (int request = 0; request < 5; request++) {
            MockHttpServletResponse response =
                    perform("GET", "/api/health", "203.0.113.13");
            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(response.getHeader("X-RateLimit-Limit")).isNull();
        }
    }

    @Test
    void optionsRequestsAreNotRateLimited() throws Exception {
        for (int request = 0; request < 5; request++) {
            MockHttpServletResponse response =
                    perform("OPTIONS", "/api/market/quote/AAPL", "203.0.113.14");
            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(response.getHeader("X-RateLimit-Limit")).isNull();
        }
    }

    private MockHttpServletResponse perform(
            String method,
            String path,
            String remoteAddress) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse)
                        .setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, filterChain);
        return response;
    }

    private RateLimitProperties propertiesWithSingleRequestLimits() {
        RateLimitProperties rateLimitProperties = new RateLimitProperties();
        rateLimitProperties.setAuthLoginPerMinute(1);
        rateLimitProperties.setAuthRegisterPerMinute(1);
        rateLimitProperties.setMarketQuotePerMinute(1);
        rateLimitProperties.setMarketSearchPerMinute(1);
        rateLimitProperties.setOrdersPerMinute(1);
        rateLimitProperties.setWatchlistPerMinute(1);
        rateLimitProperties.setDefaultPerMinute(1);
        return rateLimitProperties;
    }
}
