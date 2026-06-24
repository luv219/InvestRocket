package com.investrocket.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.admin.dto.AdminAuditLogResponse;
import com.investrocket.admin.dto.AdminDashboardStatsResponse;
import com.investrocket.admin.dto.AdminMarketDataStatusResponse;
import com.investrocket.admin.dto.AdminSystemHealthResponse;
import com.investrocket.admin.dto.AdminTradingStatsResponse;
import com.investrocket.common.ApiResponse;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    public AdminMonitoringController(AdminMonitoringService adminMonitoringService) {
        this.adminMonitoringService = adminMonitoringService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardStatsResponse> getDashboard() {
        return ApiResponse.success(
                "Admin dashboard fetched successfully",
                adminMonitoringService.getDashboardStats());
    }

    @GetMapping("/trading-stats")
    public ApiResponse<AdminTradingStatsResponse> getTradingStats() {
        return ApiResponse.success(
                "Admin trading statistics fetched successfully",
                adminMonitoringService.getTradingStats());
    }

    @GetMapping("/system-health")
    public ApiResponse<AdminSystemHealthResponse> getSystemHealth() {
        return ApiResponse.success(
                "System health fetched successfully",
                adminMonitoringService.getSystemHealth());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AdminAuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String userEmail) {
        return ApiResponse.success(
                "Admin audit logs fetched successfully",
                adminMonitoringService.getRecentAuditLogs(category, userEmail));
    }

    @GetMapping("/market-data-status")
    public ApiResponse<AdminMarketDataStatusResponse> getMarketDataStatus() {
        return ApiResponse.success(
                "Market data status fetched successfully",
                adminMonitoringService.getMarketDataProviderStatus());
    }
}
