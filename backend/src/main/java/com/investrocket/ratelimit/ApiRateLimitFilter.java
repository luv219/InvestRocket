package com.investrocket.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final String LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String RESET_HEADER = "X-RateLimit-Reset";

    private final ObjectMapper objectMapper;
    private final RateLimitProperties properties;
    private final Cache<String, Bucket> buckets;

    public ApiRateLimitFilter(
            ObjectMapper objectMapper,
            RateLimitProperties properties) {
        this(
                objectMapper,
                properties,
                Caffeine.newBuilder()
                        .maximumSize(100_000)
                        .expireAfterAccess(Duration.ofMinutes(10))
                        .build());
    }

    ApiRateLimitFilter(
            ObjectMapper objectMapper,
            RateLimitProperties properties,
            Cache<String, Bucket> buckets) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.buckets = buckets;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !properties.isEnabled()
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || "/api/health".equals(path)
                || "/ws".equals(path)
                || path.startsWith("/ws/")
                || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        RateLimitPolicy policy = resolvePolicy(request);
        String bucketKey = policy.name() + ":" + resolveClientKey(request);
        Bucket bucket = buckets.get(
                bucketKey,
                ignored -> RateLimitBucketFactory.perMinute(policy.requestsPerMinute()));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader(LIMIT_HEADER, Integer.toString(policy.requestsPerMinute()));
        response.setHeader(REMAINING_HEADER, Long.toString(probe.getRemainingTokens()));
        response.setHeader(
                RESET_HEADER,
                Long.toString(resetEpochSeconds(probe)));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(
                1,
                TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new RateLimitErrorResponse(
                false,
                "Rate limit exceeded",
                Map.of(),
                Instant.now()));
    }

    private RateLimitPolicy resolvePolicy(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("/api/auth/login".equals(path)) {
            return new RateLimitPolicy("auth-login", properties.getAuthLoginPerMinute());
        }
        if ("/api/auth/register".equals(path)) {
            return new RateLimitPolicy("auth-register", properties.getAuthRegisterPerMinute());
        }
        if (path.startsWith("/api/market/quote/")) {
            return new RateLimitPolicy("market-quote", properties.getMarketQuotePerMinute());
        }
        if ("/api/market/search".equals(path)) {
            return new RateLimitPolicy("market-search", properties.getMarketSearchPerMinute());
        }
        if (path.startsWith("/api/orders")) {
            return new RateLimitPolicy("orders", properties.getOrdersPerMinute());
        }
        if (path.startsWith("/api/watchlist")) {
            return new RateLimitPolicy("watchlist", properties.getWatchlistPerMinute());
        }
        return new RateLimitPolicy("default", properties.getDefaultPerMinute());
    }

    private String resolveClientKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/")) {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                return "user:" + authentication.getName();
            }
        }
        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private long resetEpochSeconds(ConsumptionProbe probe) {
        if (!probe.isConsumed()) {
            long waitSeconds = Math.max(
                    1,
                    TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
            return Instant.now().getEpochSecond() + waitSeconds;
        }
        return Instant.now().plusSeconds(60).getEpochSecond();
    }

    private record RateLimitPolicy(String name, int requestsPerMinute) {
    }

    private record RateLimitErrorResponse(
            boolean success,
            String message,
            Map<String, String> errors,
            Instant timestamp) {
    }
}
