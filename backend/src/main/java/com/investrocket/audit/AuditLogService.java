package com.investrocket.audit;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.investrocket.audit.dto.ActivityLogResponse;
import com.investrocket.exception.InvalidAuditCategoryException;
import com.investrocket.user.User;

@Service
public class AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final TransactionTemplate transactionTemplate;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.transactionTemplate = null;
    }

    @Autowired
    public AuditLogService(
            AuditLogRepository auditLogRepository,
            PlatformTransactionManager transactionManager) {
        this.auditLogRepository = auditLogRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void log(
            User user,
            AuditCategory category,
            AuditAction action,
            String description) {
        logWithMetadata(user, category, action, description, null);
    }

    public void logWithinCurrentTransaction(
            User user,
            AuditCategory category,
            AuditAction action,
            String description) {
        try {
            auditLogRepository.save(new AuditLog(
                    user,
                    category,
                    action,
                    description,
                    null));
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to persist audit event {} for user {}", action, user.getId());
        }
    }

    public void logWithMetadata(
            User user,
            AuditCategory category,
            AuditAction action,
            String description,
        String metadata) {
        try {
            AuditLog auditLog =
                    new AuditLog(user, category, action, description, metadata);
            if (transactionTemplate == null) {
                auditLogRepository.save(auditLog);
            } else {
                transactionTemplate.executeWithoutResult(
                        status -> auditLogRepository.saveAndFlush(auditLog));
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Unable to persist audit event {} for user {}", action, user.getId());
        }
    }

    public List<ActivityLogResponse> getMyActivity(User currentUser) {
        return auditLogRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(ActivityLogResponse::from)
                .toList();
    }

    public List<ActivityLogResponse> getMyActivityByCategory(
            User currentUser,
            String category) {
        AuditCategory parsedCategory;
        try {
            parsedCategory = AuditCategory.valueOf(
                    category.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuditCategoryException();
        }
        return auditLogRepository
                .findByUserAndCategoryOrderByCreatedAtDesc(currentUser, parsedCategory)
                .stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}
