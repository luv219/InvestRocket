package com.investrocket.alert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investrocket.user.User;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, UUID> {
    List<PriceAlert> findByUserOrderByCreatedAtDesc(User user);
    List<PriceAlert> findByUserAndStatusOrderByCreatedAtDesc(User user, PriceAlertStatus status);
    List<PriceAlert> findByStatus(PriceAlertStatus status);
    List<PriceAlert> findByStatusAndSymbol(PriceAlertStatus status, String symbol);
    Optional<PriceAlert> findByIdAndUser(UUID id, User user);
}
