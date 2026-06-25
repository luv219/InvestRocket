package com.investrocket.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    ApiRateLimitFilter apiRateLimitFilter(
            ObjectMapper objectMapper,
            RateLimitProperties properties) {
        return new ApiRateLimitFilter(objectMapper, properties);
    }

    @Bean
    FilterRegistrationBean<ApiRateLimitFilter> apiRateLimitFilterRegistration(
            ApiRateLimitFilter filter) {
        FilterRegistrationBean<ApiRateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    ProviderRateLimiter providerRateLimiter(RateLimitProperties properties) {
        return new ProviderRateLimiter(properties);
    }
}
