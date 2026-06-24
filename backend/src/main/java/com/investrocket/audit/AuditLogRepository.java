package com.investrocket.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investrocket.user.User;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);

    List<AuditLog> findByUserAndCategoryOrderByCreatedAtDesc(
            User user,
            AuditCategory category);

    List<AuditLog> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user,
            Instant start,
            Instant end);
}
