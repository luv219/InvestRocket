package com.investrocket.watchlist;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.investrocket.audit.AuditAction;
import com.investrocket.audit.AuditCategory;
import com.investrocket.audit.AuditLogService;
import com.investrocket.exception.DuplicateWatchlistItemException;
import com.investrocket.exception.InvalidMarketDataRequestException;
import com.investrocket.exception.WatchlistItemNotFoundException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;
import com.investrocket.user.User;
import com.investrocket.watchlist.dto.WatchlistItemResponse;

@Service
public class WatchlistService {

    private static final Pattern VALID_SYMBOL = Pattern.compile("[A-Z0-9.-]{1,15}");

    private final WatchlistRepository watchlistRepository;
    private final MarketDataService marketDataService;
    private AuditLogService auditLogService;

    public WatchlistService(
            WatchlistRepository watchlistRepository,
            MarketDataService marketDataService) {
        this.watchlistRepository = watchlistRepository;
        this.marketDataService = marketDataService;
    }

    @Autowired
    void setAuditLogService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Transactional
    public WatchlistItemResponse addToWatchlist(String symbol, User currentUser) {
        String normalizedSymbol = normalize(symbol);
        if (watchlistRepository.existsByUserAndSymbol(currentUser, normalizedSymbol)) {
            throw new DuplicateWatchlistItemException(normalizedSymbol);
        }

        StockQuoteResponse quote = marketDataService.getQuote(normalizedSymbol);
        StockSearchResult searchResult = marketDataService.searchStocks(normalizedSymbol)
                .stream()
                .filter(result -> result.symbol().equalsIgnoreCase(normalizedSymbol))
                .findFirst()
                .orElse(null);
        WatchlistItem item = new WatchlistItem(
                currentUser,
                normalizedSymbol,
                quote.companyName(),
                searchResult == null ? null : searchResult.exchange(),
                quote.currency());
        try {
            WatchlistItem savedItem = watchlistRepository.saveAndFlush(item);
            if (auditLogService != null) {
                auditLogService.log(
                        currentUser,
                        AuditCategory.WATCHLIST,
                        AuditAction.WATCHLIST_ADDED,
                        normalizedSymbol + " added to watchlist");
            }
            return toResponse(savedItem, quote);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateWatchlistItemException(normalizedSymbol);
        }
    }

    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> getWatchlist(User currentUser) {
        return watchlistRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(item -> toResponse(
                        item,
                        marketDataService.getQuote(item.getSymbol())))
                .toList();
    }

    @Transactional
    public void removeFromWatchlist(String symbol, User currentUser) {
        String normalizedSymbol = normalize(symbol);
        WatchlistItem item = watchlistRepository
                .findByUserAndSymbol(currentUser, normalizedSymbol)
                .orElseThrow(() -> new WatchlistItemNotFoundException(normalizedSymbol));
        watchlistRepository.delete(item);
        if (auditLogService != null) {
            auditLogService.log(
                    currentUser,
                    AuditCategory.WATCHLIST,
                    AuditAction.WATCHLIST_REMOVED,
                    normalizedSymbol + " removed from watchlist");
        }
    }

    private String normalize(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidMarketDataRequestException("Stock symbol is required");
        }
        String normalizedSymbol = symbol.trim().toUpperCase(Locale.ROOT);
        if (!VALID_SYMBOL.matcher(normalizedSymbol).matches()) {
            throw new InvalidMarketDataRequestException("Stock symbol is invalid");
        }
        return normalizedSymbol;
    }

    private WatchlistItemResponse toResponse(
            WatchlistItem item,
            StockQuoteResponse quote) {
        return new WatchlistItemResponse(
                item.getId(),
                item.getSymbol(),
                item.getCompanyName(),
                item.getExchange(),
                item.getCurrency(),
                quote.currentPrice(),
                quote.changeAmount(),
                quote.changePercent(),
                quote.latestTradingTime(),
                item.getCreatedAt());
    }
}
