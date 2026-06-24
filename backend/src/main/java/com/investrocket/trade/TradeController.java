package com.investrocket.trade;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.trade.dto.TradeResponse;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;
    private final CurrentUserService currentUserService;

    public TradeController(TradeService tradeService, CurrentUserService currentUserService) {
        this.tradeService = tradeService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<TradeResponse>> getTrades(Principal principal) {
        return ApiResponse.success(
                "Trade history retrieved successfully",
                tradeService.getTrades(currentUserService.requireUser(principal.getName())));
    }
}
