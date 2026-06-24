package com.investrocket.admin.dto;

import com.investrocket.user.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,
        @NotNull(message = "Role is required")
        Role role,
        boolean enabled) {
}
