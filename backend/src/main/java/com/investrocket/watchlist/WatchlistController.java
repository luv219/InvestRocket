package com.investrocket.watchlist;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.User;
import com.investrocket.watchlist.dto.AddWatchlistRequest;
import com.investrocket.watchlist.dto.WatchlistItemResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final CurrentUserService currentUserService;

    public WatchlistController(
            WatchlistService watchlistService,
            CurrentUserService currentUserService) {
        this.watchlistService = watchlistService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<WatchlistItemResponse>> getWatchlist(Principal principal) {
        return ApiResponse.success(
                "Watchlist fetched successfully",
                watchlistService.getWatchlist(currentUser(principal)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WatchlistItemResponse>> addToWatchlist(
            @Valid @RequestBody AddWatchlistRequest request,
            Principal principal) {
        WatchlistItemResponse item =
                watchlistService.addToWatchlist(request.symbol(), currentUser(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock added to watchlist", item));
    }

    @DeleteMapping("/{symbol}")
    public ApiResponse<Void> removeFromWatchlist(
            @PathVariable String symbol,
            Principal principal) {
        watchlistService.removeFromWatchlist(symbol, currentUser(principal));
        return ApiResponse.success("Stock removed from watchlist", null);
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
