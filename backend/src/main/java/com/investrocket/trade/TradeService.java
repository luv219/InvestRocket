package com.investrocket.trade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investrocket.trade.dto.TradeResponse;
import com.investrocket.user.User;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTrades(User currentUser) {
        return tradeRepository.findByUserOrderByExecutedAtDesc(currentUser).stream()
                .map(TradeResponse::from)
                .toList();
    }
}
