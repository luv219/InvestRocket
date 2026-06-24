package com.investrocket.audit.dto;

import java.time.Instant;
import java.util.UUID;

import com.investrocket.audit.AuditLog;

public record ActivityLogResponse(
        UUID id,
        String category,
        String action,
        String description,
        String metadata,
        Instant createdAt) {

    public static ActivityLogResponse from(AuditLog auditLog) {
        return new ActivityLogResponse(
                auditLog.getId(),
                auditLog.getCategory().name(),
                auditLog.getAction().name(),
                auditLog.getDescription(),
                auditLog.getMetadata(),
                auditLog.getCreatedAt());
    }
}
