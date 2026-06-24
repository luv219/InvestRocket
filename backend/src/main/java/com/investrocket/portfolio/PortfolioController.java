package com.investrocket.portfolio;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.portfolio.dto.HoldingResponse;
import com.investrocket.portfolio.dto.PortfolioSummaryResponse;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public PortfolioController(
            PortfolioService portfolioService,
            CurrentUserService currentUserService) {
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/holdings")
    public ApiResponse<List<HoldingResponse>> getHoldings(Principal principal) {
        return ApiResponse.success(
                "Portfolio holdings retrieved successfully",
                portfolioService.getHoldings(currentUserService.requireUser(principal.getName())));
    }

    @GetMapping("/summary")
    public ApiResponse<PortfolioSummaryResponse> getSummary(Principal principal) {
        return ApiResponse.success(
                "Portfolio summary retrieved successfully",
                portfolioService.getPortfolioSummary(
                        currentUserService.requireUser(principal.getName())));
    }
}
