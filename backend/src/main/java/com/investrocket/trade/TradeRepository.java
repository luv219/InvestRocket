package com.investrocket.trade;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investrocket.user.User;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    List<Trade> findByUserOrderByExecutedAtDesc(User user);

    long countByUser(User user);

    List<Trade> findTop20ByOrderByExecutedAtDesc();
}
