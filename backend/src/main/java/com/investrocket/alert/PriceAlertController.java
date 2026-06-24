package com.investrocket.alert;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.alert.dto.CreatePriceAlertRequest;
import com.investrocket.alert.dto.PriceAlertResponse;
import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final CurrentUserService currentUserService;

    public PriceAlertController(
            PriceAlertService priceAlertService,
            CurrentUserService currentUserService) {
        this.priceAlertService = priceAlertService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriceAlertResponse>> create(
            @Valid @RequestBody CreatePriceAlertRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Price alert created",
                priceAlertService.createAlert(request, currentUser(principal))));
    }

    @GetMapping
    public ApiResponse<List<PriceAlertResponse>> getAlerts(Principal principal) {
        return ApiResponse.success("Price alerts fetched successfully",
                priceAlertService.getMyAlerts(currentUser(principal)));
    }

    @GetMapping("/active")
    public ApiResponse<List<PriceAlertResponse>> getActive(Principal principal) {
        return ApiResponse.success("Active price alerts fetched successfully",
                priceAlertService.getActiveAlerts(currentUser(principal)));
    }

    @DeleteMapping("/{alertId}/cancel")
    public ApiResponse<PriceAlertResponse> cancel(
            @PathVariable UUID alertId,
            Principal principal) {
        return ApiResponse.success("Price alert cancelled",
                priceAlertService.cancelAlert(alertId, currentUser(principal)));
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
