package com.investrocket.marketdata;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;

@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/search")
    public ApiResponse<List<StockSearchResult>> searchStocks(
            @RequestParam String query) {
        return ApiResponse.success(
                "Stock search completed successfully",
                marketDataService.searchStocks(query));
    }

    @GetMapping("/quote/{symbol}")
    public ApiResponse<StockQuoteResponse> getQuote(@PathVariable String symbol) {
        return ApiResponse.success(
                "Quote fetched successfully",
                marketDataService.getQuote(symbol));
    }
}
