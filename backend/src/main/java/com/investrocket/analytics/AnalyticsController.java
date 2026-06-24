package com.investrocket.analytics;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.analytics.dto.AllocationResponse;
import com.investrocket.analytics.dto.HoldingPerformanceResponse;
import com.investrocket.analytics.dto.PortfolioAnalyticsResponse;
import com.investrocket.analytics.dto.PortfolioPerformancePoint;
import com.investrocket.analytics.dto.TradingStatsResponse;
import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.User;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserService currentUserService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            CurrentUserService currentUserService) {
        this.analyticsService = analyticsService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/overview")
    public ApiResponse<PortfolioAnalyticsResponse> getOverview(Principal principal) {
        return ApiResponse.success(
                "Portfolio analytics fetched successfully",
                analyticsService.getPortfolioAnalytics(currentUser(principal)));
    }

    @GetMapping("/performance")
    public ApiResponse<List<PortfolioPerformancePoint>> getPerformance(Principal principal) {
        return ApiResponse.success(
                "Portfolio performance history fetched successfully",
                analyticsService.getPerformanceHistory(currentUser(principal)));
    }

    @GetMapping("/allocation")
    public ApiResponse<List<AllocationResponse>> getAllocation(Principal principal) {
        return ApiResponse.success(
                "Portfolio allocation fetched successfully",
                analyticsService.getAllocation(currentUser(principal)));
    }

    @GetMapping("/holdings")
    public ApiResponse<List<HoldingPerformanceResponse>> getHoldings(Principal principal) {
        return ApiResponse.success(
                "Holding performance fetched successfully",
                analyticsService.getHoldingPerformance(currentUser(principal)));
    }

    @GetMapping("/trading-stats")
    public ApiResponse<TradingStatsResponse> getTradingStats(Principal principal) {
        return ApiResponse.success(
                "Trading statistics fetched successfully",
                analyticsService.getTradingStats(currentUser(principal)));
    }

    @PostMapping("/snapshot")
    public ResponseEntity<ApiResponse<PortfolioPerformancePoint>> createSnapshot(
            Principal principal) {
        PortfolioPerformancePoint snapshot =
                analyticsService.createSnapshot(currentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Portfolio snapshot created successfully", snapshot));
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
