package com.investrocket.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.investrocket.user.User;

public record UserProfileResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        String phoneNumber,
        String country,
        String preferredCurrency,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhoneNumber(),
                user.getCountry(),
                user.getPreferredCurrency(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt());
    }
}
