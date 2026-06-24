package com.investrocket.analytics;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investrocket.user.User;

public interface PortfolioSnapshotRepository
        extends JpaRepository<PortfolioSnapshot, UUID> {

    List<PortfolioSnapshot> findByUserOrderBySnapshotTimeAsc(User user);

    List<PortfolioSnapshot> findByUserOrderBySnapshotTimeDesc(User user);

    List<PortfolioSnapshot> findByUserAndSnapshotDate(User user, LocalDate snapshotDate);

    Optional<PortfolioSnapshot> findTopByUserOrderBySnapshotTimeDesc(User user);

    List<PortfolioSnapshot> findByUserAndSnapshotTimeBetweenOrderBySnapshotTimeAsc(
            User user,
            Instant start,
            Instant end);
}
