package com.investrocket.user;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.dto.RiskSettingsResponse;
import com.investrocket.user.dto.UpdateRiskSettingsRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile/risk-settings")
public class RiskSettingsController {

    private final RiskSettingsService riskSettingsService;
    private final CurrentUserService currentUserService;

    public RiskSettingsController(
            RiskSettingsService riskSettingsService,
            CurrentUserService currentUserService) {
        this.riskSettingsService = riskSettingsService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<RiskSettingsResponse> getRiskSettings(Principal principal) {
        return ApiResponse.success(
                "Risk settings fetched successfully",
                riskSettingsService.getRiskSettings(currentUser(principal)));
    }

    @PutMapping
    public ApiResponse<RiskSettingsResponse> updateRiskSettings(
            @Valid @RequestBody UpdateRiskSettingsRequest request,
            Principal principal) {
        return ApiResponse.success(
                "Risk settings updated successfully",
                riskSettingsService.updateRiskSettings(request, currentUser(principal)));
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
