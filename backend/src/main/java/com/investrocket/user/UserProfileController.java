package com.investrocket.user;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investrocket.common.ApiResponse;
import com.investrocket.common.CurrentUserService;
import com.investrocket.user.dto.ChangePasswordRequest;
import com.investrocket.user.dto.ResetAccountRequest;
import com.investrocket.user.dto.UpdateProfileRequest;
import com.investrocket.user.dto.UserProfileResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final CurrentUserService currentUserService;

    public UserProfileController(
            UserProfileService userProfileService,
            CurrentUserService currentUserService) {
        this.userProfileService = userProfileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> getProfile(Principal principal) {
        return ApiResponse.success(
                "Profile fetched successfully",
                userProfileService.getCurrentProfile(currentUser(principal)));
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Principal principal) {
        return ApiResponse.success(
                "Profile updated successfully",
                userProfileService.updateProfile(request, currentUser(principal)));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        userProfileService.changePassword(request, currentUser(principal));
        return ApiResponse.success("Password changed successfully", null);
    }

    @PostMapping("/reset-simulator")
    public ApiResponse<Void> resetSimulator(
            @Valid @RequestBody ResetAccountRequest request,
            Principal principal) {
        userProfileService.resetSimulator(request, currentUser(principal));
        return ApiResponse.success("Simulator reset successfully", null);
    }

    private User currentUser(Principal principal) {
        return currentUserService.requireUser(principal.getName());
    }
}
