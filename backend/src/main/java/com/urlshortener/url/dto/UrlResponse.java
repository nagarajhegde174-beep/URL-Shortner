package com.urlshortener.url.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UrlResponse {
    private Long id;
    private String shortCode;
    private String longUrl;
    private String shortUrl; // Full redirect URL
    private boolean isCustom;
    private boolean isActive;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private long clickCount;
}
