package com.investrocket.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.exception.InsufficientFundsException;
import com.investrocket.exception.InsufficientHoldingsException;
import com.investrocket.exception.InvalidOrderRequestException;
import com.investrocket.exception.OrderCancellationException;
import com.investrocket.exception.OrderNotFoundException;
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
        validateRequest(request);
        String symbol = request.symbol().trim().toUpperCase(Locale.ROOT);
        StockQuoteResponse quote = marketDataService.getQuote(symbol);
        BigDecimal currentPrice = scalePrice(quote.currentPrice());

        if (shouldExecuteImmediately(request, currentPrice)) {
            Order order = executeImmediateOrder(request, currentUser, quote, currentPrice);
            return OrderResponse.from(order, executionMessage(request.side()));
        }

        Order pendingOrder = createPendingOrder(request, currentUser, quote, currentPrice);
        return OrderResponse.from(pendingOrder, "Order created and is pending");
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(User currentUser) {
        return orderRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(order -> OrderResponse.from(order, order.getStatusReason()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getPendingOrders(User currentUser) {
        return orderRepository
                .findByUserAndStatusOrderByCreatedAtDesc(currentUser, OrderStatus.PENDING)
                .stream()
                .map(order -> OrderResponse.from(order, "Pending"))
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, User currentUser) {
        Order order = orderRepository.findForUpdateById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new OrderNotFoundException();
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderCancellationException("Only pending orders can be cancelled");
        }

        releaseReservation(order);
        order.cancel();
        return OrderResponse.from(order, "Pending order cancelled successfully");
    }

    @Transactional
    public Optional<OrderResponse> executePendingOrder(UUID orderId) {
        Order order = orderRepository.findForUpdateById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            return Optional.empty();
        }

        StockQuoteResponse quote = marketDataService.getQuote(order.getSymbol());
        BigDecimal currentPrice = scalePrice(quote.currentPrice());
        if (!isTriggered(order, currentPrice)) {
            return Optional.empty();
        }

        executeReservedOrder(order, quote, currentPrice);
        return Optional.of(OrderResponse.from(order, executionMessage(order.getSide())));
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request.orderType() == OrderType.MARKET) {
            if (request.limitPrice() != null || request.stopPrice() != null) {
                throw new InvalidOrderRequestException(
                        "MARKET orders must not include limitPrice or stopPrice");
            }
            return;
        }

        if (request.orderType() == OrderType.LIMIT) {
            if (!isPositive(request.limitPrice())) {
                throw new InvalidOrderRequestException(
                        "LIMIT orders require limitPrice greater than 0");
            }
            if (request.stopPrice() != null) {
                throw new InvalidOrderRequestException(
                        "LIMIT orders must not include stopPrice");
            }
            return;
        }

        if (request.side() != OrderSide.SELL) {
            throw new InvalidOrderRequestException(
                    "STOP_LOSS orders are supported for SELL only in Phase 4");
        }
        if (!isPositive(request.stopPrice())) {
            throw new InvalidOrderRequestException(
                    "STOP_LOSS orders require stopPrice greater than 0");
        }
        if (request.limitPrice() != null) {
            throw new InvalidOrderRequestException(
                    "STOP_LOSS orders must not include limitPrice");
        }
    }

    private boolean shouldExecuteImmediately(
            CreateOrderRequest request,
            BigDecimal currentPrice) {
        return request.orderType() == OrderType.MARKET
                || request.orderType() == OrderType.LIMIT
                    && (request.side() == OrderSide.BUY
                        ? currentPrice.compareTo(request.limitPrice()) <= 0
                        : currentPrice.compareTo(request.limitPrice()) >= 0)
                || request.orderType() == OrderType.STOP_LOSS
                    && currentPrice.compareTo(request.stopPrice()) <= 0;
    }

    private Order executeImmediateOrder(
            CreateOrderRequest request,
            User user,
            StockQuoteResponse quote,
            BigDecimal executionPrice) {
        BigDecimal totalAmount = amount(executionPrice, request.quantity());
        BigDecimal realizedProfitLoss = request.side() == OrderSide.BUY
                ? executeImmediateBuy(user, quote, request.quantity(), totalAmount)
                : executeImmediateSell(
                        user,
                        quote.symbol(),
                        request.quantity(),
                        executionPrice,
                        totalAmount);

        Order order = orderRepository.save(Order.executed(
                user,
                quote.symbol(),
                request.side(),
                request.orderType(),
                request.quantity(),
                executionPrice,
                scaleNullable(request.limitPrice()),
                scaleNullable(request.stopPrice()),
                executionPrice,
                totalAmount));
        createTrade(order, realizedProfitLoss);
        return order;
    }

    private Order createPendingOrder(
            CreateOrderRequest request,
            User user,
            StockQuoteResponse quote,
            BigDecimal currentPrice) {
        BigDecimal reservationAmount;
        if (request.side() == OrderSide.BUY) {
            reservationAmount = amount(request.limitPrice(), request.quantity());
            Wallet wallet = walletRepository.findForUpdateByUser(user)
                    .orElseThrow(WalletNotFoundException::new);
            if (wallet.getCashBalance().compareTo(reservationAmount) < 0) {
                throw new InsufficientFundsException();
            }
            wallet.reserve(reservationAmount);
        } else {
            reservationAmount = amount(
                    request.orderType() == OrderType.LIMIT
                            ? request.limitPrice()
                            : request.stopPrice(),
                    request.quantity());
            Holding holding = holdingRepository
                    .findForUpdateByUserAndSymbol(user, quote.symbol())
                    .orElseThrow(InsufficientHoldingsException::new);
            if (holding.getAvailableQuantity() < request.quantity()) {
                throw new InsufficientHoldingsException();
            }
            holding.lock(request.quantity());
        }

        return orderRepository.save(Order.pending(
                user,
                quote.symbol(),
                request.side(),
                request.orderType(),
                request.quantity(),
                currentPrice,
                scaleNullable(request.limitPrice()),
                scaleNullable(request.stopPrice()),
                reservationAmount));
    }

    private void executeReservedOrder(
            Order order,
            StockQuoteResponse quote,
            BigDecimal executionPrice) {
        BigDecimal totalAmount = amount(executionPrice, order.getQuantity());
        BigDecimal realizedProfitLoss;

        if (order.getSide() == OrderSide.BUY) {
            Wallet wallet = walletRepository.findForUpdateByUser(order.getUser())
                    .orElseThrow(WalletNotFoundException::new);
            BigDecimal reservedAmount = order.getTotalAmount();
            wallet.settleReserved(reservedAmount, totalAmount);
            addHolding(order.getUser(), quote, order.getQuantity(), executionPrice);
            realizedProfitLoss = BigDecimal.ZERO.setScale(2);
        } else {
            Wallet wallet = walletRepository.findForUpdateByUser(order.getUser())
                    .orElseThrow(WalletNotFoundException::new);
            Holding holding = holdingRepository
                    .findForUpdateByUserAndSymbol(order.getUser(), order.getSymbol())
                    .orElseThrow(InsufficientHoldingsException::new);
            if (holding.getLockedQuantity() < order.getQuantity()) {
                throw new InsufficientHoldingsException();
            }
            realizedProfitLoss = realizedProfitLoss(
                    executionPrice,
                    holding.getAverageBuyPrice(),
                    order.getQuantity());
            wallet.credit(totalAmount);
            holding.executeLockedSale(order.getQuantity());
            if (holding.getQuantity() == 0) {
                holdingRepository.delete(holding);
            }
        }

        order.markExecuted(executionPrice, totalAmount);
        createTrade(order, realizedProfitLoss);
    }

    private BigDecimal executeImmediateBuy(
            User user,
            StockQuoteResponse quote,
            Integer quantity,
            BigDecimal totalAmount) {
        Wallet wallet = walletRepository.findForUpdateByUser(user)
                .orElseThrow(WalletNotFoundException::new);
        if (wallet.getCashBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientFundsException();
        }
        wallet.debit(totalAmount);
        addHolding(user, quote, quantity, scalePrice(quote.currentPrice()));
        return BigDecimal.ZERO.setScale(2);
    }

    private BigDecimal executeImmediateSell(
            User user,
            String symbol,
            Integer quantity,
            BigDecimal executionPrice,
            BigDecimal totalAmount) {
        Wallet wallet = walletRepository.findForUpdateByUser(user)
                .orElseThrow(WalletNotFoundException::new);
        Holding holding = holdingRepository.findForUpdateByUserAndSymbol(user, symbol)
                .orElseThrow(InsufficientHoldingsException::new);
        if (holding.getAvailableQuantity() < quantity) {
            throw new InsufficientHoldingsException();
        }

        BigDecimal realizedProfitLoss = realizedProfitLoss(
                executionPrice,
                holding.getAverageBuyPrice(),
                quantity);
        wallet.credit(totalAmount);
        holding.remove(quantity);
        if (holding.getQuantity() == 0) {
            holdingRepository.delete(holding);
        }
        return realizedProfitLoss;
    }

    private void addHolding(
            User user,
            StockQuoteResponse quote,
            Integer quantity,
            BigDecimal executionPrice) {
        Holding holding = holdingRepository
                .findForUpdateByUserAndSymbol(user, quote.symbol())
                .orElse(null);
        if (holding == null) {
            holdingRepository.save(new Holding(
                    user,
                    quote.symbol(),
                    quote.companyName(),
                    quantity,
                    executionPrice));
        } else {
            holding.add(quantity, executionPrice);
        }
    }

    private void releaseReservation(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            Wallet wallet = walletRepository.findForUpdateByUser(order.getUser())
                    .orElseThrow(WalletNotFoundException::new);
            wallet.releaseReserved(order.getTotalAmount());
            return;
        }

        Holding holding = holdingRepository
                .findForUpdateByUserAndSymbol(order.getUser(), order.getSymbol())
                .orElseThrow(InsufficientHoldingsException::new);
        holding.unlock(order.getQuantity());
    }

    private boolean isTriggered(Order order, BigDecimal currentPrice) {
        if (order.getOrderType() == OrderType.LIMIT) {
            return order.getSide() == OrderSide.BUY
                    ? currentPrice.compareTo(order.getLimitPrice()) <= 0
                    : currentPrice.compareTo(order.getLimitPrice()) >= 0;
        }
        return order.getOrderType() == OrderType.STOP_LOSS
                && currentPrice.compareTo(order.getStopPrice()) <= 0;
    }

    private void createTrade(Order order, BigDecimal realizedProfitLoss) {
        tradeRepository.save(new Trade(
                order,
                order.getUser(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getExecutedPrice(),
                order.getTotalAmount(),
                realizedProfitLoss));
    }

    private BigDecimal realizedProfitLoss(
            BigDecimal sellPrice,
            BigDecimal averageBuyPrice,
            Integer quantity) {
        return sellPrice.subtract(averageBuyPrice)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal amount(BigDecimal price, Integer quantity) {
        return price.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scalePrice(BigDecimal price) {
        return price.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleNullable(BigDecimal price) {
        return price == null ? null : scalePrice(price);
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private String executionMessage(OrderSide side) {
        return side == OrderSide.BUY
                ? "Buy order executed successfully"
                : "Sell order executed successfully";
    }
}
