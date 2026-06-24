package com.investrocket.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.exception.WalletNotFoundException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.portfolio.dto.HoldingResponse;
import com.investrocket.portfolio.dto.PortfolioSummaryResponse;
import com.investrocket.user.User;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@Service
public class PortfolioService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final HoldingRepository holdingRepository;
    private final WalletRepository walletRepository;
    private final MarketDataService marketDataService;

    public PortfolioService(
            HoldingRepository holdingRepository,
            WalletRepository walletRepository,
            MarketDataService marketDataService) {
        this.holdingRepository = holdingRepository;
        this.walletRepository = walletRepository;
        this.marketDataService = marketDataService;
    }

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldings(User currentUser) {
        return holdingRepository.findByUser(currentUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(User currentUser) {
        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(WalletNotFoundException::new);
        List<HoldingResponse> holdings = getHoldings(currentUser);

        BigDecimal holdingsValue = holdings.stream()
                .map(HoldingResponse::currentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInvested = holdings.stream()
                .map(HoldingResponse::totalInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedProfitLoss = holdingsValue.subtract(totalInvested)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPercent = percentage(unrealizedProfitLoss, totalInvested);

        return new PortfolioSummaryResponse(
                wallet.getCashBalance(),
                holdingsValue,
                wallet.getCashBalance().add(holdingsValue).setScale(2, RoundingMode.HALF_UP),
                totalInvested,
                unrealizedProfitLoss,
                unrealizedPercent,
                holdings.size());
    }

    private HoldingResponse toResponse(Holding holding) {
        StockQuoteResponse quote = marketDataService.getQuote(holding.getSymbol());
        BigDecimal currentValue = quote.currentPrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedProfitLoss = currentValue.subtract(holding.getTotalInvested())
                .setScale(2, RoundingMode.HALF_UP);

        return new HoldingResponse(
                holding.getSymbol(),
                holding.getCompanyName(),
                holding.getQuantity(),
                holding.getAverageBuyPrice(),
                quote.currentPrice(),
                holding.getTotalInvested(),
                currentValue,
                unrealizedProfitLoss,
                percentage(unrealizedProfitLoss, holding.getTotalInvested()));
    }

    private BigDecimal percentage(BigDecimal profitLoss, BigDecimal invested) {
        if (invested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return profitLoss.multiply(ONE_HUNDRED)
                .divide(invested, 2, RoundingMode.HALF_UP);
    }
}
