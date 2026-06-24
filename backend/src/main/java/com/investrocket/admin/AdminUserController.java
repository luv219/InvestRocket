package com.investrocket.admin;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.admin.dto.AdminUserDetailResponse;
import com.investrocket.admin.dto.AdminUserResponse;
import com.investrocket.admin.dto.AdminUserUpdateRequest;
import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final CurrentUserService currentUserService;

    public AdminUserController(
            AdminUserService adminUserService,
            CurrentUserService currentUserService) {
        this.adminUserService = adminUserService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<AdminUserResponse>> getUsers() {
        return ApiResponse.success(
                "Admin users fetched successfully",
                adminUserService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUser(@PathVariable UUID userId) {
        return ApiResponse.success(
                "Admin user detail fetched successfully",
                adminUserService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ApiResponse<AdminUserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserUpdateRequest request,
            Principal principal) {
        return ApiResponse.success(
                "User updated successfully",
                adminUserService.updateUser(userId, request, currentAdmin(principal)));
    }

    @PostMapping("/{userId}/disable")
    public ApiResponse<AdminUserResponse> disableUser(
            @PathVariable UUID userId,
            Principal principal) {
        return ApiResponse.success(
                "User disabled successfully",
                adminUserService.disableUser(userId, currentAdmin(principal)));
    }

    @PostMapping("/{userId}/enable")
    public ApiResponse<AdminUserResponse> enableUser(
            @PathVariable UUID userId,
            Principal principal) {
        return ApiResponse.success(
                "User enabled successfully",
                adminUserService.enableUser(userId, currentAdmin(principal)));
    }

    private User currentAdmin(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
