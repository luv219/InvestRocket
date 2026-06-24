package com.investrocket.marketdata;

import java.util.List;

import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.marketdata.dto.StockSearchResult;

public interface MarketDataProvider {

    List<StockSearchResult> searchStocks(String query);

    StockQuoteResponse getQuote(String symbol);
}
