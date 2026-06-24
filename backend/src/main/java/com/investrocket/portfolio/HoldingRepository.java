package com.investrocket.portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.investrocket.user.User;

import jakarta.persistence.LockModeType;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {

    Optional<Holding> findByUserAndSymbol(User user, String symbol);

    List<Holding> findByUser(User user);

    long countByUser(User user);

    long deleteByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Holding> findForUpdateByUserAndSymbol(User user, String symbol);
}
