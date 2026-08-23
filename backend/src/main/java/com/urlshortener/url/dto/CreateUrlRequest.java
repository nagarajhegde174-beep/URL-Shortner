package com.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateUrlRequest {

    @NotBlank(message = "Long URL is required")
    private String longUrl;

    private String customCode;

    private Instant expiresAt;
}
