package com.investrocket.auth.dto;

import java.util.UUID;

import com.investrocket.user.Role;
import com.investrocket.user.User;

public record UserResponse(UUID id, String fullName, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
