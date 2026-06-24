package com.investrocket.admin.dto;

import java.time.Instant;
import java.util.UUID;

import com.investrocket.audit.AuditLog;

public record AdminAuditLogResponse(
        UUID id,
        String userEmail,
        String category,
        String action,
        String description,
        String metadata,
        Instant createdAt) {

    public static AdminAuditLogResponse from(AuditLog log) {
        return new AdminAuditLogResponse(
                log.getId(),
                log.getUser().getEmail(),
                log.getCategory().name(),
                log.getAction().name(),
                log.getDescription(),
                log.getMetadata(),
                log.getCreatedAt());
    }
}
