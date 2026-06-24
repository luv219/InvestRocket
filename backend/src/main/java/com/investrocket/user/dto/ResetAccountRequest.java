package com.investrocket.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetAccountRequest(
        @NotBlank(message = "Reset confirmation is required")
        String confirmText) {
}
