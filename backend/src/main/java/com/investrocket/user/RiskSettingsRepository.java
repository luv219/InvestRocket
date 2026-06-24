package com.investrocket.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskSettingsRepository extends JpaRepository<UserRiskSettings, UUID> {

    Optional<UserRiskSettings> findByUser(User user);

    boolean existsByUser(User user);
}
