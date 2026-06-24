package com.investrocket.admin.dto;

import java.util.List;

import com.investrocket.audit.dto.ActivityLogResponse;
import com.investrocket.portfolio.dto.PortfolioSummaryResponse;
import com.investrocket.user.dto.RiskSettingsResponse;
import com.investrocket.user.dto.UserProfileResponse;

public record AdminUserDetailResponse(
        UserProfileResponse profile,
        AdminUserResponse summary,
        RiskSettingsResponse riskSettings,
        PortfolioSummaryResponse portfolioSummary,
        List<AdminOrderResponse> recentOrders,
        List<AdminTradeResponse> recentTrades,
        List<ActivityLogResponse> recentActivity) {
}
