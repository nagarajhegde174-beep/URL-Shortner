package com.urlshortener.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String role;
    private String plan;
    private boolean isPro;
    private Instant subscriptionExpiresAt;
}
