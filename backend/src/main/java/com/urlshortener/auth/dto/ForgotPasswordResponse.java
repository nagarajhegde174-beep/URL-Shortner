package com.urlshortener.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the forgot-password API response.
 * Contains ONLY a generic message — never the reset token or any user-identifying information.
 * Kept for backwards compatibility; functionally equivalent to MessageResponse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;
}
