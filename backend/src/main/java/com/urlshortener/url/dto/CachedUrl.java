package com.urlshortener.url.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedUrl implements Serializable {
    private static final long serialVersionUID = 1L;
    private String longUrl;
    private boolean isActive;
    private Instant expiresAt;
}
