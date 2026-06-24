package com.investrocket.audit;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.audit.dto.ActivityLogResponse;
import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.User;

@RestController
@RequestMapping("/api/activity")
public class AuditController {

    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    public AuditController(
            AuditLogService auditLogService,
            CurrentUserService currentUserService) {
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<ActivityLogResponse>> getActivity(
            @RequestParam(required = false) String category,
            Principal principal) {
        User currentUser = currentUserService.requireUser(principal.getName());
        List<ActivityLogResponse> activity = category == null || category.isBlank()
                ? auditLogService.getMyActivity(currentUser)
                : auditLogService.getMyActivityByCategory(currentUser, category);
        return ApiResponse.success("Activity history fetched successfully", activity);
    }
}
