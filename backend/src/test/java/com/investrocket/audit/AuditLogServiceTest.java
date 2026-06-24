package com.investrocket.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.investrocket.exception.InvalidAuditCategoryException;
import com.investrocket.user.User;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void logsSafeActivityData() {
        AuditLogService service = new AuditLogService(auditLogRepository);
        User user = new User("Demo User", "demo@example.com", "hash");

        service.log(
                user,
                AuditCategory.PROFILE,
                AuditAction.PROFILE_UPDATED,
                "Profile information updated");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void filtersActivityByCategory() {
        AuditLogService service = new AuditLogService(auditLogRepository);
        User user = new User("Demo User", "demo@example.com", "hash");
        when(auditLogRepository.findByUserAndCategoryOrderByCreatedAtDesc(
                user,
                AuditCategory.ORDER))
                .thenReturn(List.of());

        assertThat(service.getMyActivityByCategory(user, "order")).isEmpty();
    }

    @Test
    void rejectsUnknownCategory() {
        AuditLogService service = new AuditLogService(auditLogRepository);
        User user = new User("Demo User", "demo@example.com", "hash");

        assertThatThrownBy(() -> service.getMyActivityByCategory(user, "unknown"))
                .isInstanceOf(InvalidAuditCategoryException.class);
    }
}
