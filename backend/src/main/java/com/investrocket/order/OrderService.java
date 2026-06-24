package com.investrocket.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.exception.InsufficientFundsException;
import com.investrocket.exception.InsufficientHoldingsException;
import com.investrocket.exception.UnsupportedOrderTypeException;
import com.investrocket.exception.WalletNotFoundException;
import com.investrocket.marketdata.MarketDataService;
import com.investrocket.marketdata.dto.StockQuoteResponse;
import com.investrocket.order.dto.CreateOrderRequest;
import com.investrocket.order.dto.OrderResponse;
import com.investrocket.portfolio.Holding;
import com.investrocket.portfolio.HoldingRepository;
import com.investrocket.trade.Trade;
import com.investrocket.trade.TradeRepository;
import com.investrocket.user.User;
import com.investrocket.wallet.Wallet;
import com.investrocket.wallet.WalletRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final HoldingRepository holdingRepository;
    private final WalletRepository walletRepository;
    private final MarketDataService marketDataService;

    public OrderService(
            OrderRepository orderRepository,
            TradeRepository tradeRepository,
            HoldingRepository holdingRepository,
            WalletRepository walletRepository,
            MarketDataService marketDataService) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.holdingRepository = holdingRepository;
        this.walletRepository = walletRepository;
        this.marketDataService = marketDataService;
    }

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request, User currentUser) {
        if (request.orderType() != OrderType.MARKET) {
            throw new UnsupportedOrderTypeException();
        }

        String symbol = request.symbol().trim().toUpperCase(Locale.ROOT);
        StockQuoteResponse quote = marketDataService.getQuote(symbol);
        BigDecimal executionPrice = quote.currentPrice().setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalAmount = executionPrice
                .multiply(BigDecimal.valueOf(request.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
        Wallet wallet = walletRepository.findForUpdateByUser(currentUser)
                .orElseThrow(WalletNotFoundException::new);

        BigDecimal realizedProfitLoss = request.side() == OrderSide.BUY
                ? executeMarketBuy(currentUser, quote, request.quantity(), totalAmount, wallet)
                : executeMarketSell(currentUser, symbol, request.quantity(), executionPrice, totalAmount, wallet);

        Order order = orderRepository.save(new Order(
                currentUser,
                symbol,
                request.side(),
                request.orderType(),
                request.quantity(),
                executionPrice,
                executionPrice,
                totalAmount));
        tradeRepository.save(new Trade(
                order,
                currentUser,
                symbol,
                request.side(),
                request.quantity(),
                executionPrice,
                totalAmount,
                realizedProfitLoss));

        String message = request.side() == OrderSide.BUY
                ? "Market buy order executed successfully"
                : "Market sell order executed successfully";
        return OrderResponse.from(order, message);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(User currentUser) {
        return orderRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(order -> OrderResponse.from(order, "Executed"))
                .toList();
    }

    private BigDecimal executeMarketBuy(
            User user,
            StockQuoteResponse quote,
            Integer quantity,
            BigDecimal totalAmount,
            Wallet wallet) {
        if (wallet.getCashBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException();
        }

        wallet.debit(totalAmount);
        Holding holding = holdingRepository.findForUpdateByUserAndSymbol(user, quote.symbol())
                .orElse(null);
        if (holding == null) {
            holdingRepository.save(new Holding(
                    user,
                    quote.symbol(),
                    quote.companyName(),
                    quantity,
                    quote.currentPrice()));
        } else {
            holding.add(quantity, quote.currentPrice());
        }
        return BigDecimal.ZERO.setScale(2);
    }

    private BigDecimal executeMarketSell(
            User user,
            String symbol,
            Integer quantity,
            BigDecimal executionPrice,
            BigDecimal totalAmount,
            Wallet wallet) {
        Holding holding = holdingRepository.findForUpdateByUserAndSymbol(user, symbol)
                .orElseThrow(InsufficientHoldingsException::new);
        if (holding.getQuantity() < quantity) {
            throw new InsufficientHoldingsException();
        }

        BigDecimal realizedProfitLoss = executionPrice
                .subtract(holding.getAverageBuyPrice())
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        wallet.credit(totalAmount);
        holding.remove(quantity);
        if (holding.getQuantity() == 0) {
            holdingRepository.delete(holding);
        }
        return realizedProfitLoss;
    }
}
