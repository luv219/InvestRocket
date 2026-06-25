package com.investrocket.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int authLoginPerMinute = 5;
    private int authRegisterPerMinute = 3;
    private int marketQuotePerMinute = 30;
    private int marketSearchPerMinute = 20;
    private int ordersPerMinute = 20;
    private int watchlistPerMinute = 60;
    private int defaultPerMinute = 120;
    private int finnhubPerMinute = 50;
    private int twelveDataPerMinute = 8;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAuthLoginPerMinute() {
        return authLoginPerMinute;
    }

    public void setAuthLoginPerMinute(int authLoginPerMinute) {
        this.authLoginPerMinute = authLoginPerMinute;
    }

    public int getAuthRegisterPerMinute() {
        return authRegisterPerMinute;
    }

    public void setAuthRegisterPerMinute(int authRegisterPerMinute) {
        this.authRegisterPerMinute = authRegisterPerMinute;
    }

    public int getMarketQuotePerMinute() {
        return marketQuotePerMinute;
    }

    public void setMarketQuotePerMinute(int marketQuotePerMinute) {
        this.marketQuotePerMinute = marketQuotePerMinute;
    }

    public int getMarketSearchPerMinute() {
        return marketSearchPerMinute;
    }

    public void setMarketSearchPerMinute(int marketSearchPerMinute) {
        this.marketSearchPerMinute = marketSearchPerMinute;
    }

    public int getOrdersPerMinute() {
        return ordersPerMinute;
    }

    public void setOrdersPerMinute(int ordersPerMinute) {
        this.ordersPerMinute = ordersPerMinute;
    }

    public int getWatchlistPerMinute() {
        return watchlistPerMinute;
    }

    public void setWatchlistPerMinute(int watchlistPerMinute) {
        this.watchlistPerMinute = watchlistPerMinute;
    }

    public int getDefaultPerMinute() {
        return defaultPerMinute;
    }

    public void setDefaultPerMinute(int defaultPerMinute) {
        this.defaultPerMinute = defaultPerMinute;
    }

    public int getFinnhubPerMinute() {
        return finnhubPerMinute;
    }

    public void setFinnhubPerMinute(int finnhubPerMinute) {
        this.finnhubPerMinute = finnhubPerMinute;
    }

    public int getTwelveDataPerMinute() {
        return twelveDataPerMinute;
    }

    public void setTwelveDataPerMinute(int twelveDataPerMinute) {
        this.twelveDataPerMinute = twelveDataPerMinute;
    }
}
