package com.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateUrlRequest {

    @NotBlank(message = "Long URL is required")
    private String longUrl;

    private Instant expiresAt;

    private Boolean isActive;
}
