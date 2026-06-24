package com.investrocket.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,
        @Size(max = 40, message = "Phone number must not exceed 40 characters")
        String phoneNumber,
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Preferred currency must be a 3-letter code")
        String preferredCurrency) {
}
