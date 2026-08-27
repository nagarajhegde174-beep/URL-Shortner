package com.urlshortener.analytics.dto;

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
public class ClickEventDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId; // UUID for duplicate prevention
    private Long linkId;
    private String referrer;
    private String userAgent;
    private String ipHash; // SHA-256 hashed IP
    private Instant clickedAt;
}
