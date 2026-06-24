package com.investrocket.order;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.order.dto.CreateOrderRequest;
import com.investrocket.order.dto.OrderResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderController(OrderService orderService, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal) {
        OrderResponse order = orderService.placeOrder(
                request,
                currentUserService.requireUser(principal.getName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order.message(), order));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrders(Principal principal) {
        return ApiResponse.success(
                "Order history retrieved successfully",
                orderService.getOrders(currentUserService.requireUser(principal.getName())));
    }

    @GetMapping("/pending")
    public ApiResponse<List<OrderResponse>> getPendingOrders(Principal principal) {
        return ApiResponse.success(
                "Pending orders retrieved successfully",
                orderService.getPendingOrders(
                        currentUserService.requireUser(principal.getName())));
    }

    @DeleteMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            Principal principal) {
        return ApiResponse.success(
                "Pending order cancelled successfully",
                orderService.cancelOrder(
                        orderId,
                        currentUserService.requireUser(principal.getName())));
    }
}
